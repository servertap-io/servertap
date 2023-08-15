package io.servertap.webhooks.models.events;

import com.google.gson.annotations.Expose;
import io.servertap.api.v1.models.Player;

public class PlayerJoinWebhookEvent extends WebhookEvent {
    @Expose
    Player player;

    @Expose
    String joinMessage;

    public PlayerJoinWebhookEvent() {
        eventType = EventType.PlayerJoin;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }
}
