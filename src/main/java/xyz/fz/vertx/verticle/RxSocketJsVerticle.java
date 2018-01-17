package xyz.fz.vertx.verticle;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;
import rx.Observable;
import rx.Subscription;
import xyz.fz.vertx.util.BaseProperties;

public class RxSocketJsVerticle extends AbstractVerticle {

    static final String SOCKET_MESSAGE_ADDRESS = "socketMessage";

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.route("/" + SOCKET_MESSAGE_ADDRESS + "/*").handler(SockJSHandler.create(vertx).socketHandler(sockJSSocket -> {

            sockJSSocket.handler(buffer -> {
                System.out.println(buffer.toString());
            });

            // Consumer the event bus address as an Observable
            Observable<String> msg = vertx.eventBus().<String>consumer(SOCKET_MESSAGE_ADDRESS)
                    .bodyStream()
                    .toObservable();

            // Send the event to the client
            Subscription subscription = msg.subscribe(sockJSSocket::write);

            // Unsubscribe when the socket closes
            sockJSSocket.endHandler(v -> {
                subscription.unsubscribe();
            });
        }));

        vertx.createHttpServer().requestHandler(router::accept).listen(Integer.parseInt(BaseProperties.get("socket.js.port")));
    }
}
