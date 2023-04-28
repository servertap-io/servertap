package io.servertap.plugin.api;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.websocket.WsConfig;
import io.servertap.PluginEntrypoint;

import java.util.function.Consumer;

public class ServerTapWebserverServiceImpl implements ServerTapWebserverService {

    private final Javalin javalin;

    public ServerTapWebserverServiceImpl(PluginEntrypoint main) {
        this.javalin = main.getJavalin();
    }

    @Override
    public Javalin getWebserver() {
        return javalin;
    }

    @Override
    public void get(String path, Handler handler) {
        javalin.get(path, handler);
    }

    @Override
    public void post(String path, Handler handler) {
        javalin.post(path, handler);
    }

    @Override
    public void put(String path, Handler handler) {
        javalin.put(path, handler);
    }

    @Override
    public void delete(String path, Handler handler) {
        javalin.delete(path, handler);
    }

    @Override
    public void websocket(String path, Consumer<WsConfig> handler) {
        javalin.ws(path, handler);
    }
}
