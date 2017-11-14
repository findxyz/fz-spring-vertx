package xyz.fz.vertx;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import xyz.fz.vertx.verticle.AbcVerticle;
import xyz.fz.vertx.verticle.HttpServerVerticle;

public class Application {

    private static final String SCAN_PACKAGES = "xyz.fz.vertx";

    // 集群初始化失败的情况下，可以使用默认vertx实例
    private static Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.scan(SCAN_PACKAGES);
        annotationConfigApplicationContext.refresh();

        // Hazelcast配置类
        Config cfg = new Config();
        // 加入组的配置，防止广播环境下，负载串到别的开发机中
        GroupConfig group = new GroupConfig();
        group.setName("vertxGroup");
        group.setPassword("vertxGroupPassword");
        cfg.setGroupConfig(group);
        // 声明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
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
            System.out.println("cluster failed, using default vertx");
            deploy(vertx);
        }
    }

    private static void deploy(Vertx vertx) {

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        vertx.deployVerticle(AbcVerticle.class.getName(), deploymentOptions);

        vertx.deployVerticle(HttpServerVerticle.class.getName());
    }
}
