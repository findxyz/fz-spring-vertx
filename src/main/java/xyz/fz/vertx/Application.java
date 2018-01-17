package xyz.fz.vertx;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import xyz.fz.vertx.util.BaseProperties;
import xyz.fz.vertx.verticle.AbcVerticle;
import xyz.fz.vertx.verticle.HttpServerVerticle;
import xyz.fz.vertx.verticle.MongoVerticle;
import xyz.fz.vertx.verticle.RxSocketJsVerticle;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Application {

    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getLogger("global").setLevel(Level.FINEST);
    }

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Application.class);

    private static final String SCAN_PACKAGES = BaseProperties.get("spring.scan.packages");

    private static String VERTX_CLUSTER_HOST = BaseProperties.get("vertx.cluster.host");

    // 集群初始化失败的情况下，可以使用默认vertx实例
    private static Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {

        if (args.length > 0) {
            VERTX_CLUSTER_HOST = args[0];
        }

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.scan(SCAN_PACKAGES);
        annotationConfigApplicationContext.refresh();

        // Hazelcast配置类
        Config cfg = new Config();
        // 加入组的配置，防止广播环境下，负载串到别的开发机中
        GroupConfig group = new GroupConfig();
        group.setName(BaseProperties.get("vertx.group.name"));
        group.setPassword(BaseProperties.get("vertx.group.password"));
        cfg.setGroupConfig(group);
        // 声明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        String clusterHost = VERTX_CLUSTER_HOST;
        VertxOptions options = new VertxOptions()
                .setClusterManager(mgr)
                .setClusterHost(clusterHost);
        // 集群化vertx
        Vertx.clusteredVertx(options, Application::resultHandler);
    }

    private static void resultHandler(AsyncResult<Vertx> res) {
        // 如果成功，使用集群化的vertx实例
        if (res.succeeded()) {
            vertx = res.result();
            // 这里要注意，一定要在异步回调中，获取了vertx实例后，再去部署模块
            // 由于vert.x所有内部逻辑都是异步调用的，所以，如果你在异步回调前就去部署模块，最终会导致集群失败
            deploy(vertx);
        } else {
            logger.error("cluster failed, using default vertx");
            deploy(vertx);
        }
    }

    private static void deploy(Vertx vertx) {
        // 部署想要部署的verticle
        // 需要先启动 mongodb 服务
        DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(8).setWorker(true);
        vertx.deployVerticle(MongoVerticle.class.getName(), deploymentOptions);

        DeploymentOptions deploymentOptions2 = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle(AbcVerticle.class.getName(), deploymentOptions2);

        vertx.deployVerticle(HttpServerVerticle.class.getName());

        vertx.deployVerticle(RxSocketJsVerticle.class.getName());
    }
}
