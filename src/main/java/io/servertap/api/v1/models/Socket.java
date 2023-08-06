package io.servertap.api.v1.models;

import io.javalin.websocket.WsConnectContext;
import io.servertap.api.v1.models.events.ClientMessage;
import io.servertap.api.v1.models.events.ServerMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Socket {
    private final String id;
    private final List<String> rooms;
    private final WsConnectContext ctx;
    private final Map<String, Consumer<ClientMessage>> events;
    private final Logger log;

    public Socket(WsConnectContext ctx, Logger log) {
        this.id = ctx.getSessionId();
        this.rooms = null;
        this.ctx = ctx;
        this.log = log;
        this.events = new ConcurrentHashMap<>();
    }

    public void on(String event, Consumer<ClientMessage> callback) {
        events.put(event, callback);
    }

    public void emit(String event, Object obj) {
        ctx.send(new ServerMessage(event, obj));
    }

    public boolean isOpen() {
        return ctx.session.isOpen();
    }

    public Map<String, Consumer<ClientMessage>> getEvents() {
        return events;
    }
}
