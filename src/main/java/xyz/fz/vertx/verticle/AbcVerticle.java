package xyz.fz.vertx.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import xyz.fz.vertx.service.AbcService;
import xyz.fz.vertx.util.SpringContextHelper;

import java.util.UUID;

public class AbcVerticle extends AbstractVerticle {

    private static final String ID = UUID.randomUUID().toString();

    static final String ABC_ADDRESS = "abcAddress";

    static final String ABC_ADDRESS_JSON = "abcAddressJson";

    @Override
    public void start() {
        AbcService abcService = SpringContextHelper.getBean("abcServiceImpl", AbcService.class);

        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ABC_ADDRESS, msg -> {
            try {
                msg.reply(abcService.hello(msg.body().toString() + ID));
            } catch (Exception e) {
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "error: " + e.getMessage());
            }
        });

        eventBus.consumer(ABC_ADDRESS_JSON, msg -> {
            try {
                JsonObject requestMap = (JsonObject) msg.body();
                msg.reply(requestMap);
            } catch (Exception e) {
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "error: " + e.getMessage());
            }
        });
    }
}
