package io.servertap.api.v1.serverSideEvents;

import io.servertap.custom.events.OperatorListUpdatedEvent;
import io.servertap.custom.events.WhitelistUpdatedEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class ServerSideEventListener implements Listener {
    private final ServerSideEventsHandler sse;
    private final BukkitScheduler scheduler;
    public ServerSideEventListener(ServerSideEventsHandler sse) {
        this.sse = sse;
        this.scheduler = Bukkit.getScheduler();
        initReversePolling();
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

    private void updatePlayerInventory(InventoryEvent event) {
        HumanEntity player = event.getView().getPlayer();
        String uuid = player.getUniqueId().toString();
        String worldUUID = player.getWorld().getUID().toString();
        sse.broadcast(String.format("%s.updateInventory", uuid), new Object());
    }

    private void updateWhitelistList() {
        sse.broadcast("updateWhitelistList", new Object());
    }

    private void updateOperatorsList() {
        sse.broadcast("updateOperatorsList", new Object());
    }

    private void initReversePolling() {
        scheduler.runTaskTimerAsynchronously();
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
    public void onInventory(InventoryEvent event) {
        updatePlayerInventory(event);
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
