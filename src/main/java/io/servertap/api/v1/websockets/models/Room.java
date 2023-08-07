package io.servertap.api.v1.websockets.models;

import io.servertap.api.v1.websockets.WebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Room {
    private final String name;
    private final List<SocketID> sockets;
    private final WebSocketHandler ws;
    private Consumer<Socket> onJoin;

    public Room(String name, WebSocketHandler webSocketHandler, Consumer<Socket> onJoin) {
        this.name = name;
        this.sockets = new ArrayList<>();
        this.ws = webSocketHandler;
        this.onJoin = onJoin;
    }

    public String getName() {
        return name;
    }

    public void join(SocketID id) {
        sockets.add(id);
        onJoin.accept(ws.to(id));
    }

    public void leave(SocketID id) {
        sockets.remove(id);
    }
    public void emit(String event, Object obj) {
        sockets.forEach(socketID -> ws.to(socketID).emit(event, obj));
    };
}
