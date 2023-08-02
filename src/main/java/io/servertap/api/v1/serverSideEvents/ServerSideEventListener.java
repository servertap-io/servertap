package io.servertap.api.v1.serverSideEvents;

import io.servertap.Constants;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import io.servertap.api.v1.models.Player;
import io.servertap.api.v1.models.events.PlayerJoinSseEvent;
import io.servertap.api.v1.models.events.PlayerKickSseEvent;
import io.servertap.api.v1.models.events.PlayerQuitSseEvent;
import io.servertap.custom.events.BanListUpdatedEvent;
import io.servertap.custom.events.IpBanListUpdatedEvent;
import io.servertap.custom.events.OperatorListUpdatedEvent;
import io.servertap.custom.events.WhitelistUpdatedEvent;
import io.servertap.utils.NormalizeMessage;
import io.servertap.utils.pluginwrappers.EconomyWrapper;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.logging.Logger;

public class ServerSideEventListener {
    private final ServerTapMain main;
    private final Logger log;
    private final ApiV1Initializer api;
    private final EconomyWrapper economy;
    private final ServerSideEventsHandler sse;
    private final BukkitScheduler scheduler;
    public ServerSideEventListener(ServerTapMain main, Logger log, ApiV1Initializer api, EconomyWrapper economy, ServerSideEventsHandler sse) {
        this.main = main;
        this.log = log;
        this.api = api;
        this.economy = economy;
        this.sse = sse;
        this.scheduler = Bukkit.getServer().getScheduler();
        registerListeners();
    }

    private void registerListeners() {
        FileConfiguration bukkitConfig = main.getConfig();
        boolean enabled = bukkitConfig.getBoolean("sse.enabled", false);

        if(!enabled)
            return;

        Map<String, Listener> eventListeners = mapListeners();
        List<String> events = bukkitConfig.getStringList("sse.enabledEvents");
        boolean updatePlayerInventory = bukkitConfig.getBoolean("sse.enabledPlayerInventoryUpdates", false);

        events.forEach((event) -> {
            if(eventListeners.containsKey(event))
                registerListener(eventListeners.get(event));
        });
        eventListeners.clear();

        if(updatePlayerInventory)
            registerListener(new updateInventoryListeners());
    }

    // Event Maps
    // Mops events to their respective listeners
    // Used to figure out if the user has enabled an event in the config and register it

    private Map<String, Listener> mapListeners() {
        Map<String, Listener> listenerMap = new HashMap<>();
        listenerMap.put(Constants.PLAYER_JOIN_EVENT, new playerJoinListener());
        listenerMap.put(Constants.PLAYER_QUIT_EVENT, new playerQuitListener());
        listenerMap.put(Constants.PLAYER_KICKED_EVENT, new playerKickedListener());
        listenerMap.put(Constants.UPDATE_ONLINE_PLAYER_LIST_EVENT, new updateOnlinePlayersListListeners());
        listenerMap.put(Constants.UPDATE_ALL_PLAYER_LIST_EVENT, new updateAllPlayersListListeners());
        listenerMap.put(Constants.UPDATE_WORLD_DATA_EVENT, new updateWorldsDataListeners());
        listenerMap.put(Constants.UPDATE_SERVER_DATA_EVENT, new updateServerDataListeners());
        listenerMap.put(Constants.UPDATE_WHITELIST_EVENT, new updateWhitelistListListeners());
        listenerMap.put(Constants.UPDATE_OPS_LIST_EVENT, new updateOperatorsListListeners());
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
        sse.broadcast(Constants.PLAYER_JOIN_EVENT, eventModel);
    }

    private void onPlayerQuitHandler(PlayerQuitEvent event) {
        PlayerQuitSseEvent eventModel = new PlayerQuitSseEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setQuitMessage(NormalizeMessage.normalize(event.getQuitMessage(), main));
        sse.broadcast(Constants.PLAYER_QUIT_EVENT, eventModel);
    }

    private void onPlayerKickHandler(PlayerKickEvent event) {
        PlayerKickSseEvent eventModel = new PlayerKickSseEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setReason(NormalizeMessage.normalize(event.getReason(), main));
        sse.broadcast(Constants.PLAYER_KICKED_EVENT, eventModel);
    }

    private void updateOnlinePlayersList(PlayerEvent event) {
        if(event.getEventName().equals("PlayerQuitEvent")) {
            ArrayList<Player> players = new ArrayList<>();
            UUID PlayerUUID = event.getPlayer().getUniqueId();
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getUniqueId() != PlayerUUID)
                    players.add(Player.fromBukkitPlayer(player, economy));
            });
            sse.broadcast(Constants.UPDATE_ONLINE_PLAYER_LIST_EVENT, players);
        } else {
            sse.broadcast(Constants.UPDATE_ONLINE_PLAYER_LIST_EVENT, api.getPlayerApi().getOninePlayers());
        }
    }

    private void updateAllPlayersList() {
        sse.broadcast(Constants.UPDATE_ALL_PLAYER_LIST_EVENT, api.getPlayerApi().getAllPlayers());
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
        sse.broadcast(Constants.UPDATE_SERVER_DATA_EVENT, api.getServerApi().getServer());
    }

    private void updateWhitelistList() {
        sse.broadcast(Constants.UPDATE_WHITELIST_EVENT, api.getServerApi().getWhitelist());
    }

    private void updateOperatorsList() {
        sse.broadcast(Constants.UPDATE_OPS_LIST_EVENT, api.getServerApi().getOpsList());
    }

    // Bukkit Event Listeners

    private class playerJoinListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) { onPlayerJoinHandler(event); }
    }

    private class playerQuitListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) { onPlayerQuitHandler(event); }
    }

    private class playerKickedListener implements Listener {
        @EventHandler
        public void onPlayerKick(PlayerKickEvent event) {
            onPlayerKickHandler(event);
        }
    }

    private class updateOnlinePlayersListListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) { updateOnlinePlayersList(event); }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) { updateOnlinePlayersList(event); }

    }

    private class updateAllPlayersListListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) { updateAllPlayersList(); }
    }

    private class updateWorldsDataListeners implements Listener {
        @EventHandler
        public void onWeatherChange(WeatherChangeEvent event) { updateWorldsInfo(); }

        @EventHandler
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            String command = event.getMessage();
            if(command.contains("/gamerule") && (command.contains("true") || command.contains("false")))
                updateWorldsInfo();
        }

        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            String command = event.getCommand();
            if(command.contains("gamerule") && (command.contains("true") || command.contains("false")))
                updateWorldsInfo();
        }
    }

    private class updateServerDataListeners implements Listener {
        @EventHandler
        public void onWhitelistUpdated(WhitelistUpdatedEvent event) { updateServerInfo(); }

        @EventHandler
        public void onOperatorListUpdated(OperatorListUpdatedEvent event) { updateServerInfo(); }

        @EventHandler
        public void onBanListUpdated(BanListUpdatedEvent event) { updateServerInfo(); }

        @EventHandler
        public void onIpBanListUpdated(IpBanListUpdatedEvent event) { updateServerInfo(); }
    }

    private class updateWhitelistListListeners implements Listener {
        @EventHandler
        public void onWhitelistUpdated(WhitelistUpdatedEvent event) { updateWhitelistList(); }
    }

    private class updateOperatorsListListeners implements Listener {
        @EventHandler
        public void onOperatorListUpdated(OperatorListUpdatedEvent event) { updateOperatorsList(); }
    }

    private class updateInventoryListeners implements Listener {
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