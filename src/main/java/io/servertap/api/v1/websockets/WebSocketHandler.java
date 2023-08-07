package io.servertap.api.v1.websockets;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.websockets.models.Room;
import io.servertap.api.v1.websockets.models.Socket;
import io.servertap.api.v1.websockets.models.SocketID;
import io.servertap.api.v1.websockets.models.events.ClientMessage;
import io.servertap.utils.ConsoleListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class WebSocketHandler {
    private final ServerTapMain main;
    private final Logger log;
    private final Map<SocketID, Socket> sockets;
    private final Map<String, Room> rooms;


    public WebSocketHandler(ServerTapMain main, Logger log, ConsoleListener consoleListener) {
        this.main = main;
        this.log = log;
        this.sockets = new ConcurrentHashMap<>();
        this.rooms = new ConcurrentHashMap<>();
        createRoom("consoleListener", (socket -> {
            for (ConsoleLine line : main.getConsoleBuffer()) socket.emit("newConsoleLine", line);
        }));
        consoleListener.addListener((consoleLine) -> to("consoleListener").emit("newConsoleLine", consoleLine));
    }

    public void getHandler(WsConfig ws) {
        ws.onConnect((ctx) -> {
            ctx.enableAutomaticPings();
            Socket socket = WebSocketConfig.configure(new Socket(ctx), main, log);
            sockets.put(socket.getID(), socket);
        });

        ws.onMessage((ctx) -> {
            Socket socket = sockets.get(new SocketID(ctx.getSessionId()));
            try {
                ClientMessage clientMsg = ctx.messageAsClass(ClientMessage.class);

                if(clientMsg.getName() == null) {
                    socket.emit("error", "Invalid JSON. Name key note found.");
                    return;
                }
                if(clientMsg.getPayload() == null) {
                    socket.emit("error", "Invalid JSON. Payload key note found.");
                    return;
                }
                if(clientMsg.getName().startsWith("join")) {
                    String room = clientMsg.getPayload();
                    if(!rooms.containsKey(room)) {
                        socket.emit("error", "That room doesn't exist!");
                        return;
                    }
                    if("joinRoom".equals(clientMsg.getName()))
                        rooms.get(room).join(socket.getID());
                    if("leaveRoom".equals(clientMsg.getName()))
                        rooms.get(room).join(socket.getID());
                    return;
                }
                socket.getEvents().get(clientMsg.getName()).accept(clientMsg);
            } catch (NullPointerException e) {
                socket.emit("error", "That event doesn't exist!");
            } catch (Exception e) {
                socket.emit("error", "Invalid JSON payload. Please check your code. [Did you call JSON.stringify() on the obj you are sending?");
            }
        });

        ws.onClose((ctx) -> sockets.remove(new SocketID(ctx.getSessionId())));

        ws.onError((ctx) -> sockets.remove(new SocketID(ctx.getSessionId())));
    }

    public void emit(String event, Object obj) {
        sockets.forEach((key, socket) -> socket.emit(event, obj));
    }

    public Socket to(SocketID id) {
        return sockets.get(id);
    }

    public Room to(String room) {
        return rooms.get(room);
    }

    public void createRoom(String name, Consumer<Socket> onJoin) {
        rooms.put(name, new Room(name, this, onJoin));
    }

    public void deleteRoom(String name) {
        rooms.remove(name);
    }
}