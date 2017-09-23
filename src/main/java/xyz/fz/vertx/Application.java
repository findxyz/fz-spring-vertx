package xyz.fz.vertx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import xyz.fz.vertx.verticle.AbcVerticle;
import xyz.fz.vertx.verticle.HttpServerVerticle;

import java.io.IOException;

public class Application {

    private static final String SCAN_PACKAGES = "xyz.fz.vertx";

    public static AnnotationConfigApplicationContext springContext;

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "json serial error";
        }
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String[] args) {

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.scan(SCAN_PACKAGES);
        annotationConfigApplicationContext.refresh();
        springContext = annotationConfigApplicationContext;

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(HttpServerVerticle.class.getName());
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        vertx.deployVerticle(AbcVerticle.class.getName(), deploymentOptions);
    }
}
