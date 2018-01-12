package xyz.fz.vertx.util;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import xyz.fz.vertx.model.Result;

public class EventBusUtil {

    public static void eventBusSend(Vertx vertx, String address, String message, final HttpServerResponse response) {
        vertx.eventBus().send(address, message, ar -> {
            if (ar.succeeded()) {
                response.end(Result.ofData(ar.result().body()));
            } else {
                response.end(Result.ofMessage(ar.cause().getMessage()));
            }
        });
    }

    public static void eventBusSend(Vertx vertx, String address, Buffer event, final HttpServerResponse response) {
        vertx.eventBus().send(address, event2JsonObject(event, response), ar -> {
            if (ar.succeeded()) {
                JsonObject responseJsonObject = (JsonObject) ar.result().body();
                response.end(Result.ofData(responseJsonObject.getMap()));
            } else {
                response.end(Result.ofMessage(ar.cause().getMessage()));
            }
        });
    }

    private static JsonObject event2JsonObject(Buffer event, HttpServerResponse response) {
        try {
            String requestJson = event.toString();
            return new JsonObject(requestJson);
        } catch (Exception e) {
            response.end(Result.ofMessage(e.getMessage()));
            throw e;
        }
    }

}
