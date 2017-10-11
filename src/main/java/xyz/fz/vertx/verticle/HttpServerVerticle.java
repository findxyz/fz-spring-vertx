package xyz.fz.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import xyz.fz.vertx.model.Result;

import java.io.IOException;
import java.util.Properties;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
    private static Properties properties;

    static {
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("application.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/*").handler(routingContext -> {
            logger.debug("/* filter");
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");
            routingContext.next();
        });

        router.route("/abc/*").handler(routingContext -> {
            logger.debug("/abc/* filter");
            routingContext.next();
        });

        router.route("/abc").handler(routingContext -> {
            String name = routingContext.request().getParam("name");
            HttpServerResponse response = routingContext.response();
            vertx.eventBus().send("abcAddress", name, ar -> {
                if (ar.succeeded()) {
                    response.end(Result.ofData(ar.result().body()));
                } else {
                    response.end(Result.ofMessage(ar.cause().getMessage()));
                }
            });
        });

        router.route("/abc/json").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            routingContext.request().bodyHandler(event -> {
                String requestJson = event.toString();
                JsonObject requestMap = new JsonObject(requestJson);
                vertx.eventBus().send("abcJson", requestMap, ar -> {
                    if (ar.succeeded()) {
                        JsonObject result = (JsonObject) ar.result().body();
                        response.end(Result.ofData(result.getMap()));
                    } else {
                        response.end(Result.ofMessage(ar.cause().getMessage()));
                    }
                });
            });
        });

        String serverPort = properties.get("server.port").toString();

        httpServer.requestHandler(router::accept).listen(Integer.parseInt(serverPort));

        logger.info("vertx httpServer started at port:{}", serverPort);
    }

}
