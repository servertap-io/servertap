package io.servertap.api.v1.websockets.models;

import io.javalin.websocket.WsConnectContext;
import io.servertap.api.v1.websockets.models.events.ClientMessage;
import io.servertap.api.v1.websockets.models.events.ServerMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Socket {
    private final SocketID id;
    private final WsConnectContext ctx;
    private final Map<String, Consumer<ClientMessage>> events;

    public Socket(WsConnectContext ctx) {
        this.id = new SocketID(ctx.getSessionId());
        this.ctx = ctx;
        this.events = new ConcurrentHashMap<>();
        ctx.enableAutomaticPings(1000);
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

    public SocketID getID() {
        return id;
    }
}
