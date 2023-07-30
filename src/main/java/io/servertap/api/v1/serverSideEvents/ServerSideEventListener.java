package io.servertap.api.v1.serverSideEvents;

import io.servertap.api.v1.models.Player;
import io.servertap.webhooks.models.events.PlayerJoinWebhookEvent;
import io.servertap.webhooks.models.events.PlayerKickWebhookEvent;
import io.servertap.webhooks.models.events.PlayerQuitWebhookEvent;
import io.servertap.webhooks.models.events.WebhookEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ServerSideEventListener implements Listener {
    private final ServerSideEventsHandler sse;
    public ServerSideEventListener(ServerSideEventsHandler sse) {
        this.sse = sse;
    }

    private void updateOnlinePlayers() {
        sse.broadcast("updateOnlinePlayers", new Object());
    }

    private void updateAllPlayers() {
        sse.broadcast("updateAllPlayers", new Object());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateOnlinePlayers();
        updateAllPlayers();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updateOnlinePlayers();
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        updateOnlinePlayers();
    }
}
