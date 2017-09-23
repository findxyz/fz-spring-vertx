package xyz.fz.vertx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import xyz.fz.vertx.verticle.AbcVerticle;
import xyz.fz.vertx.verticle.HttpServerVerticle;

public class Application {

    private static final String SCAN_PACKAGES = "xyz.fz.vertx";

    public static void main(String[] args) {

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.scan(SCAN_PACKAGES);
        annotationConfigApplicationContext.refresh();

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(HttpServerVerticle.class.getName());
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        vertx.deployVerticle(AbcVerticle.class.getName(), deploymentOptions);
        System.out.println("vertx httpServer started");
    }
}
