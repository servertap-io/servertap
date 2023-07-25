package io.servertap.webhooks.models.events;

import com.google.gson.annotations.Expose;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.Player;

import java.util.List;

public class PlayerDeathWebhookEvent extends WebhookEvent {
    @Expose
    private Player player;

    @Expose
    private String deathMessage;

    @Expose
    private List<ItemStack> drops;

    public PlayerDeathWebhookEvent() {
        eventType = EventType.PlayerDeath;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    public void setDrops(List<ItemStack> drops) {
        this.drops = drops;
    }
}
