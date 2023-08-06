package io.servertap.api.v1.serverSideEvents;

import io.servertap.Constants;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import io.servertap.api.v1.models.Player;
import io.servertap.api.v1.models.events.PlayerJoinSseEvent;
import io.servertap.api.v1.models.events.PlayerKickSseEvent;
import io.servertap.api.v1.models.events.PlayerQuitSseEvent;
import io.servertap.custom.events.BanListUpdatedAsyncEvent;
import io.servertap.custom.events.IpBanListUpdatedAsyncEvent;
import io.servertap.custom.events.OperatorListUpdatedAsyncEvent;
import io.servertap.custom.events.WhitelistUpdatedAsyncEvent;
import io.servertap.utils.NormalizeMessage;
import io.servertap.utils.pluginwrappers.EconomyWrapper;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.function.Supplier;

public class ServerSideEventListener {
    private final ServerTapMain main;
    private final ApiV1Initializer api;
    private final EconomyWrapper economy;
    private final ServerSideEventsHandler sse;
    private final BukkitScheduler scheduler;
    private final FileConfiguration bukkitConfig;
    private final boolean enabled;
    public ServerSideEventListener(ServerTapMain main, ApiV1Initializer api, EconomyWrapper economy, ServerSideEventsHandler sse) {
        this.main = main;
        this.api = api;
        this.economy = economy;
        this.sse = sse;
        this.scheduler = Bukkit.getServer().getScheduler();
        this.bukkitConfig = main.getConfig();
        this.enabled = bukkitConfig.getBoolean("sse.enabled", false);
        registerListeners();
    }

    private void registerListeners() {
        if(!enabled)
            return;

        Map<String, Supplier<Listener>> eventListeners = mapListeners();
        List<String> events = bukkitConfig.getStringList("sse.enabledEvents");
        boolean updatePlayerInventory = bukkitConfig.getBoolean("sse.enablePlayerInventoryUpdates", false);
        boolean updatePlayerLocation = bukkitConfig.getBoolean("sse.enablePlayerLocationUpdates", false);

        events.stream()
                .filter(eventListeners::containsKey)
                .distinct()
                .forEach((event) -> registerListener(eventListeners.get(event).get()));

        if(updatePlayerInventory)
            registerListener(new UpdateInventoryListeners());
        if(updatePlayerLocation)
            registerListener(new UpdatePlayerLocationListeners());
    }


    /**
     * Returns a Map Mops events to their respective listeners
     * Used to figure out if the user has enabled an event in the config and register it
     * @return ListenerMap
     */
    private Map<String, Supplier<Listener>> mapListeners() {
        Map<String, Supplier<Listener>> listenerMap = new HashMap<>();
        listenerMap.put(Constants.PLAYER_JOIN_EVENT, PlayerJoinListener::new);
        listenerMap.put(Constants.PLAYER_QUIT_EVENT, PlayerQuitListener::new);
        listenerMap.put(Constants.PLAYER_KICKED_EVENT, PlayerKickedListener::new);
        listenerMap.put(Constants.UPDATE_ONLINE_PLAYER_LIST_EVENT, UpdateOnlinePlayersListListeners::new);
        listenerMap.put(Constants.UPDATE_ALL_PLAYER_LIST_EVENT, UpdateAllPlayersListListeners::new);
        listenerMap.put(Constants.UPDATE_WORLD_DATA_EVENT, UpdateWorldsDataListeners::new);
        listenerMap.put(Constants.UPDATE_SERVER_DATA_EVENT, UpdateServerDataListeners::new);
        listenerMap.put(Constants.UPDATE_WHITELIST_EVENT, UpdateWhitelistListListeners::new);
        listenerMap.put(Constants.UPDATE_OPS_LIST_EVENT, UpdateOperatorsListListeners::new);
        return listenerMap;
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, main);
    }

    // Event Handlers

    private void runOnNextTick(Runnable callback) {
        scheduler.runTask(main, callback);
    }

    private void onPlayerJoinHandler(PlayerJoinEvent event) {
        PlayerJoinSseEvent eventModel = new PlayerJoinSseEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setJoinMessage(NormalizeMessage.normalize(event.getJoinMessage(), main));
        runOnNextTick(() -> sse.broadcast(Constants.PLAYER_JOIN_EVENT, eventModel));
    }

    private void onPlayerQuitHandler(PlayerQuitEvent event) {
        PlayerQuitSseEvent eventModel = new PlayerQuitSseEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setQuitMessage(NormalizeMessage.normalize(event.getQuitMessage(), main));
        runOnNextTick(() -> sse.broadcast(Constants.PLAYER_QUIT_EVENT, eventModel));
    }

    private void onPlayerKickHandler(PlayerKickEvent event) {
        PlayerKickSseEvent eventModel = new PlayerKickSseEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setReason(NormalizeMessage.normalize(event.getReason(), main));
        runOnNextTick(() -> sse.broadcast(Constants.PLAYER_KICKED_EVENT, eventModel));
    }

    private void updateOnlinePlayersList(PlayerEvent event) {
        runOnNextTick(() -> sse.broadcast(Constants.UPDATE_ONLINE_PLAYER_LIST_EVENT, api.getPlayerApi().getOninePlayers()));
    }

    private void updateAllPlayersList() {
        runOnNextTick(() -> sse.broadcast(Constants.UPDATE_ALL_PLAYER_LIST_EVENT, api.getPlayerApi().getAllPlayers()));
    }

    private void updateWorldsInfo() {
        runOnNextTick(() -> sse.broadcast(Constants.UPDATE_WORLD_DATA_EVENT, api.getWorldApi().getWorlds()));
    }

    private void updatePlayerInventory(HumanEntity player) {
        UUID playerUUID = player.getUniqueId();
        UUID worldUUID = player.getWorld().getUID();
        runOnNextTick(() -> sse.broadcast(String.format("%s.%s", playerUUID, Constants.UPDATE_PLAYER_INVENTORY_EVENT_POSTFIX), api.getPlayerApi().getPlayerInv(playerUUID, worldUUID)));
    }

    private void updateServerInfo() {
        runOnNextTick(() -> sse.broadcast(Constants.UPDATE_SERVER_DATA_EVENT, api.getServerApi().getServer()));
    }

    private void updateWhitelistList() {
        runOnNextTick(() -> sse.broadcast(Constants.UPDATE_WHITELIST_EVENT, api.getServerApi().getWhitelist()));
    }

    private void updateOperatorsList() {
        runOnNextTick(() -> sse.broadcast(Constants.UPDATE_OPS_LIST_EVENT, api.getServerApi().getOpsList()));
    }

    // Bukkit Event Listeners

    private class PlayerJoinListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) { onPlayerJoinHandler(event); }
    }

    private class PlayerQuitListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) { onPlayerQuitHandler(event); }
    }

    private class PlayerKickedListener implements Listener {
        @EventHandler
        public void onPlayerKick(PlayerKickEvent event) {
            onPlayerKickHandler(event);
        }
    }

    private class UpdateOnlinePlayersListListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) { updateOnlinePlayersList(null); }
        @EventHandler
        public void onOperatorListUpdated(OperatorListUpdatedAsyncEvent event) { updateOnlinePlayersList(null); }
        @EventHandler
        public void onPlayerChangedWorld(PlayerChangedWorldEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) { updateOnlinePlayersList(null); }
        @EventHandler
        public void onEntityRegainHealth(EntityRegainHealthEvent event) { updateOnlinePlayersList(null); }
    }

    private class UpdatePlayerLocationListeners implements Listener {
        @EventHandler
        public void onMove(PlayerMoveEvent event) { updateOnlinePlayersList(event); }
    }

    private class UpdateAllPlayersListListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) { updateAllPlayersList(); }
    }

    private class UpdateWorldsDataListeners implements Listener {
        @EventHandler
        public void onWeatherChange(WeatherChangeEvent event) { updateWorldsInfo(); }

        @EventHandler
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            String command = event.getMessage();
            if(command.contains("/gamerule") && (command.contains("true") || command.contains("false")))
                updateWorldsInfo();
            if(command.contains("/difficulty") && command.length() > 11)
                updateWorldsInfo();
        }

        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            String command = event.getCommand();
            if(command.contains("gamerule") && (command.contains("true") || command.contains("false")))
                updateWorldsInfo();
            if(command.contains("difficulty") && command.length() > 11)
                updateWorldsInfo();
        }
    }

    private class UpdateServerDataListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) { updateServerInfo(); }
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) { updateServerInfo(); }
        @EventHandler
        public void onWhitelistUpdated(WhitelistUpdatedAsyncEvent event) { updateServerInfo(); }

        @EventHandler
        public void onBanListUpdated(BanListUpdatedAsyncEvent event) { updateServerInfo(); }

        @EventHandler
        public void onIpBanListUpdated(IpBanListUpdatedAsyncEvent event) { updateServerInfo(); }
    }

    private class UpdateWhitelistListListeners implements Listener {
        @EventHandler
        public void onWhitelistUpdated(WhitelistUpdatedAsyncEvent event) { updateWhitelistList(); }
    }

    private class UpdateOperatorsListListeners implements Listener {
        @EventHandler
        public void onOperatorListUpdated(OperatorListUpdatedAsyncEvent event) { updateOperatorsList(); }
    }

    private class UpdateInventoryListeners implements Listener {
        @EventHandler
        public void onInventoryClickEvent(InventoryClickEvent event) {
            updatePlayerInventory(event.getView().getPlayer());
        }

        @EventHandler
        public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
            updatePlayerInventory((HumanEntity) event.getEntity());
        }

        @EventHandler
        public void onBlockDispenseArmorEvent(BlockDispenseArmorEvent event) {
            updatePlayerInventory((HumanEntity) event.getTargetEntity());
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            updatePlayerInventory(event.getPlayer());
        }

        @EventHandler
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            String command = event.getMessage();
            if(command.contains("/give") || command.contains("/clear"))
                updatePlayerInventory(event.getPlayer());
        }

        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            String command = event.getCommand();
            if(command.contains("give") || command.contains("clear"))
                updatePlayerInventory(Objects.requireNonNull(Bukkit.getPlayer(command.substring(command.indexOf(' '), command.indexOf(' ', command.length())))));
        }
    }
}