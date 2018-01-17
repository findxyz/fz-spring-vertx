package xyz.fz.vertx.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import xyz.fz.vertx.model.Result;

public class EventBusUtil {

    public static void eventBusSend(Vertx vertx, String address, String message, final HttpServerResponse response) {
        vertx.eventBus().send(address, message, ar -> {
            handleResponse(ar, response);
        });
    }

    public static void eventBusSend(Vertx vertx, String address, Buffer event, final HttpServerResponse response) {
        JsonObject eventJsonObject = event2JsonObject(event);
        if (eventJsonObject != null) {
            vertx.eventBus().send(address, eventJsonObject, ar -> {
                handleResponse(ar, response);
            });
        } else {
            response.end(Result.ofMessage(null));
        }
    }

    private static void handleResponse(AsyncResult<Message<Object>> ar, HttpServerResponse response) {
        if (ar.succeeded()) {
            Object resObject = ar.result().body();
            if (resObject != null) {
                if (resObject instanceof JsonObject) {
                    JsonObject responseJsonObject = (JsonObject) resObject;
                    response.end(Result.ofData(responseJsonObject.getMap()));
                } else {
                    response.end(Result.ofData(resObject));
                }
            } else {
                response.end(Result.ofSuccess());
            }
        } else {
            response.end(Result.ofMessage(ar.cause().getMessage()));
        }
    }

    private static JsonObject event2JsonObject(Buffer event) {
        try {
            String requestJson = event.toString();
            return new JsonObject(requestJson);
        } catch (Exception e) {
            return null;
        }
    }

}
