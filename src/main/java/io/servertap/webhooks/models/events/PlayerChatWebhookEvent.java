package io.servertap.webhooks.models.events;

import com.google.gson.annotations.Expose;
import io.servertap.api.v1.models.Player;

public class PlayerChatWebhookEvent extends WebhookEvent {
    @Expose
    private Player player;

    @Expose
    String playerName;

    @Expose
    String message;

    public PlayerChatWebhookEvent() {
        eventType = EventType.PlayerChat;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
