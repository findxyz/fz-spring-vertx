package xyz.fz.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;


public class TcpClientVerticle extends AbstractVerticle {

    @Override
    public void start() {
        NetClient tcpClient = vertx.createNetClient();
        tcpClient.connect(80, "jenkov.com",
                result -> {
                    NetSocket socket = result.result();
                    socket.write("GET / HTTP/1.1\r\nHost: jenkov.com\r\n\r\n");
                    socket.handler(buffer -> {
                        System.out.println("Received data: " + buffer.length());
                        System.out.println(buffer.getString(0, buffer.length()));
                    });
                });
    }
}
