package io.servertap.api.v1.models.events;

import com.google.gson.annotations.Expose;

public class WebhookEvent {
    public enum EventType {PlayerJoin, PlayerQuit, PlayerKick, PlayerChat, PlayerDeath}

    @Expose
    protected EventType eventType;
}
