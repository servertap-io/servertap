package io.servertap.api.v1.serverSideEvents;

import io.javalin.http.sse.SseClient;
import io.javalin.http.sse.SseHandler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerSideEventsHandler {
    private final Queue<SseClient> clients;

    public ServerSideEventsHandler() {
        this.clients= new ConcurrentLinkedQueue<>();
    }

    public SseHandler getHandler() {
        return new SseHandler(client -> {
            client.keepAlive();
            clients.add(client);
            client.onClose(clients::remove);
        });
    }

    public void broadcast(String eventName, Object obj) {
        for (SseClient client : clients)
            client.sendEvent(eventName, obj);
    }
}
