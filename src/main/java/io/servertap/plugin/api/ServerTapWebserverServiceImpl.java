package io.servertap.plugin.api;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.websocket.WsConfig;
import io.servertap.ServerTapMain;
import io.servertap.WebServer;

import java.util.function.Consumer;

public class ServerTapWebserverServiceImpl implements ServerTapWebserverService {

    private final WebServer webServer;

    public ServerTapWebserverServiceImpl(ServerTapMain main) {
        this.webServer = main.getWebServer();
    }

    @Override
    public Javalin getWebserver() {
        return webServer.getJavalin();
    }

    @Override
    public void get(String path, Handler handler) {
        webServer.get(path, handler);
    }

    @Override
    public void post(String path, Handler handler) {
        webServer.post(path, handler);
    }

    @Override
    public void put(String path, Handler handler) {
        webServer.put(path, handler);
    }

    @Override
    public void delete(String path, Handler handler) {
        webServer.delete(path, handler);
    }

    @Override
    public void websocket(String path, Consumer<WsConfig> handler) {
        webServer.ws(path, handler);
    }
}
