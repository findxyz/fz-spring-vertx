package xyz.fz.vertx.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import xyz.fz.vertx.Application;
import xyz.fz.vertx.service.AbcService;

public class AbcVerticle extends AbstractVerticle {

    @Override
    public void start() {
        AbcService abcService = (AbcService) Application.springContext.getBean("abcServiceImpl");

        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("abcAddress", msg -> {
            try {
                msg.reply(abcService.hello(msg.body().toString()));
            } catch (Exception e) {
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "error: " + e.getMessage());
            }
        });
    }
}
