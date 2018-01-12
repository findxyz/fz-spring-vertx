package xyz.fz.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.vertx.model.Result;
import xyz.fz.vertx.util.BaseProperties;
import xyz.fz.vertx.util.BaseUtil;
import xyz.fz.vertx.util.EventBusUtil;

import static xyz.fz.vertx.verticle.AbcVerticle.ABC_ADDRESS;
import static xyz.fz.vertx.verticle.AbcVerticle.ABC_ADDRESS_JSON;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().failureHandler(routingContext -> {
            Throwable failure = routingContext.failure();
            logger.error(BaseUtil.getExceptionStackTrace(failure));
            routingContext.response().end(Result.ofMessage(failure.getMessage()));
        });

        // filter
        router.route("/*").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");
            routingContext.next();
        });

        /*
        router.route("/abc/*").handler(routingContext -> {
            logger.debug("/abc/* filter");
            routingContext.next();
        });
        */

        router.route("/abc").handler(routingContext -> {
            String name = routingContext.request().getParam("name");
            HttpServerResponse response = routingContext.response();
            EventBusUtil.eventBusSend(vertx, ABC_ADDRESS, name, response);
        });

        router.route("/abc/json").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            routingContext.request().bodyHandler(event -> {
                EventBusUtil.eventBusSend(vertx, ABC_ADDRESS_JSON, event, response);
            });
        });

        String serverPort = BaseProperties.get("server.port");

        httpServer.requestHandler(router::accept).listen(Integer.parseInt(serverPort));

        logger.info("vertx httpServer started at port:{}", serverPort);
    }

}
