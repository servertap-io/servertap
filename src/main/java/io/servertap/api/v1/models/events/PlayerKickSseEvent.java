package io.servertap.api.v1.models.events;

import com.google.gson.annotations.Expose;
import io.servertap.api.v1.models.Player;
import io.servertap.webhooks.models.events.WebhookEvent;

public class PlayerKickSseEvent extends WebhookEvent {
    @Expose
    Player player;

    @Expose
    String reason;

    public PlayerKickSseEvent() {
        eventType = EventType.PlayerKick;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
