package xyz.fz.vertx.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import xyz.fz.vertx.service.AbcService;
import xyz.fz.vertx.util.SpringContextHelper;

public class AbcVerticle extends AbstractVerticle {

    @Override
    public void start() {
        AbcService abcService = SpringContextHelper.getBean("abcServiceImpl", AbcService.class);

        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("abcAddress", msg -> {
            try {
                msg.reply(abcService.hello(msg.body().toString()));
            } catch (Exception e) {
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "error: " + e.getMessage());
            }
        });

        eventBus.consumer("abcJson", msg -> {
            try {
                JsonObject requestMap = (JsonObject) msg.body();
                msg.reply(requestMap);
            } catch (Exception e) {
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "error: " + e.getMessage());
            }
        });
    }
}
