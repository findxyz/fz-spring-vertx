package xyz.fz.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import xyz.fz.vertx.util.BaseUtil;

public class HttpServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/abc").handler(routingContext -> {
            String something = routingContext.request().getParam("name");
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");
            vertx.eventBus().send("abcAddress", something, ar -> {
                if (ar.succeeded()) {
                    response.end(BaseUtil.toJson(ar.result().body()));
                } else {
                    response.end(ar.cause().getMessage());
                }
            });
        });

        httpServer.requestHandler(router::accept).listen(6666);
    }

}
