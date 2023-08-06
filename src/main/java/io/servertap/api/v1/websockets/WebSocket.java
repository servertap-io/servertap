package io.servertap.api.v1.websockets;

import io.javalin.websocket.WsConfig;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.models.Socket;
import io.servertap.api.v1.models.events.ClientMessage;
import io.servertap.api.v1.models.events.ServerMessage;
import io.servertap.utils.ConsoleListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class WebSocket {
    private final ServerTapMain main;
    private final Logger log;
    private final Map<String, Socket> sockets;

    public WebSocket(ServerTapMain main, Logger log, ConsoleListener consoleListener) {
        this.main = main;
        this.log = log;
        sockets = new ConcurrentHashMap<>();
        consoleListener.addListener(this::emit);
    }

    public void getHandler(WsConfig ws) {
        ws.onConnect((ctx) -> {
            Socket socket = WebSocketConfig.configure(new Socket(ctx, log), main, log);
            sockets.put(ctx.getSessionId(), socket);
            ctx.enableAutomaticPings(30 * 1000);
            for (ConsoleLine line : main.getConsoleBuffer()) socket.emit("newConsoleLine", line);
        });

        ws.onMessage((ctx) -> {
            Socket socket = sockets.get(ctx.getSessionId());
            try {
                ClientMessage clientMsg = ctx.messageAsClass(ClientMessage.class);

                if(clientMsg.getName() == null) {
                    ctx.send(new ServerMessage("error", "Invalid JSON. Name key note found."));
                    return;
                }
                if(clientMsg.getPayload() == null) {
                    ctx.send(new ServerMessage("error", "Invalid JSON. Payload key note found."));
                    return;
                }

                socket.getEvents().get(clientMsg.getName()).accept(clientMsg);
            } catch (NullPointerException e) {
                ctx.send(new ServerMessage("error", "That event doesn't exist!"));
            } catch (Exception e) {
                ctx.send(new ServerMessage("error", "Invalid JSON payload. Please check your code. [Did you call JSON.stringify() on the obj you are sending?"));
            }
        });

        ws.onClose((ctx) -> sockets.remove(ctx.getSessionId()));

        ws.onError((ctx) -> sockets.remove(ctx.getSessionId()));
    }

    public void emit(String event, Object obj) {
        sockets.forEach((key, socket) -> socket.emit(event, obj));
    }
}