package xyz.fz.vertx.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.vertx.util.BaseProperties;
import xyz.fz.vertx.util.BaseUtil;

public class MongoVerticle extends AbcVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MongoVerticle.class);

    static final String MONGO_ADDRESS_SAVE = "mongoSave";

    static final String MONGO_ADDRESS_FIND = "mongoFind";

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String KEY_CONTENT = "content";

    private static final String KEY_DATE = "date";

    private static final String KEY_DONE = "done";

    private static final String COLLECTION_MESSAGES = "messages";

    @Override
    public void start() {
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", BaseProperties.get("mongodb.uri"))
                .put("db_name", BaseProperties.get("mongodb.name"));

        MongoClient mongoClient = MongoClient.createShared(vertx, mongoConfig);

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(MONGO_ADDRESS_SAVE, msg -> {
            try {
                JsonObject requestMap = (JsonObject) msg.body();
                JsonObject saveMessage = new JsonObject();
                saveMessage.put(KEY_CONTENT, requestMap.getString(KEY_CONTENT));
                saveMessage.put(KEY_DATE, DateTime.now().toString(DATE_PATTERN));
                saveMessage.put(KEY_DONE, 0);
                mongoClient.save(COLLECTION_MESSAGES, saveMessage, id -> {
                    msg.reply(null);
                });
            } catch (Exception e) {
                logger.error(BaseUtil.getExceptionStackTrace(e));
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), BaseUtil.errorFormat(e.getMessage()));
            }
        });

        eventBus.consumer(MONGO_ADDRESS_FIND, msg -> {
            try {
                mongoClient.findWithOptions(COLLECTION_MESSAGES, new JsonObject().put(KEY_DONE, 0), new FindOptions().setSort(new JsonObject().put("_id", 1)).setLimit(1), res -> {
                    if (res.result().size() > 0) {
                        JsonObject oneMessage = res.result().get(0);
                        String id = oneMessage.getString("_id");
                        mongoClient.updateCollection(COLLECTION_MESSAGES, new JsonObject().put("_id", id), new JsonObject().put("$set", new JsonObject().put(KEY_DONE, 1)), rs -> {
                            JsonObject resMessage = new JsonObject();
                            resMessage.put(KEY_CONTENT, oneMessage.getString(KEY_CONTENT));
                            resMessage.put(KEY_DATE, oneMessage.getString(KEY_DATE));
                            msg.reply(resMessage);
                        });
                    } else {
                        // done random message
                        JsonObject randomMessage = new JsonObject();
                        randomMessage.put(KEY_CONTENT, "random");
                        randomMessage.put(KEY_DATE, DateTime.now().toString(DATE_PATTERN));
                        msg.reply(randomMessage);
                    }
                });
            } catch (Exception e) {
                logger.error(BaseUtil.getExceptionStackTrace(e));
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), BaseUtil.errorFormat(e.getMessage()));
            }
        });
    }
}
