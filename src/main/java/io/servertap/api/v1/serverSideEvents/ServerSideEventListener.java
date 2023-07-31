package io.servertap.api.v1.serverSideEvents;

import io.servertap.custom.events.OperatorListUpdatedEvent;
import io.servertap.custom.events.ServerUpdatedEvent;
import io.servertap.custom.events.WhitelistUpdatedEvent;

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

    private void updateOnlinePlayersList() {
        sse.broadcast("updateOnlinePlayersList", new Object());
    }

    private void updateAllPlayersList() {
        sse.broadcast("updateAllPlayersList", new Object());
    }

    private void updateServerInfo() {
        sse.broadcast("updateServerInfo", new Object());
    }

    private void updateWhitelistList() {
        sse.broadcast("updateWhitelistList", new Object());
    }

    private void updateOperatorsList() {
        sse.broadcast("updateOperatorsList", new Object());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateOnlinePlayersList();
        updateAllPlayersList();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updateOnlinePlayersList();
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        updateOnlinePlayersList();
    }

    @EventHandler
    public void onServerUpdated(ServerUpdatedEvent event) {
        updateServerInfo();
    }

    @EventHandler
    public void onWhitelistUpdated(WhitelistUpdatedEvent event) {
        updateServerInfo();
        updateWhitelistList();
    }

    @EventHandler
    public void onOperatorListUpdated(OperatorListUpdatedEvent event) {
        updateServerInfo();
        updateOperatorsList();
    }
}
