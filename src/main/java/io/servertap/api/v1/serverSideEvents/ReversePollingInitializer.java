package io.servertap.api.v1.serverSideEvents;

import io.servertap.Constants;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.Logger;

public class ReversePollingInitializer {
    private final int DEFAULT_REFRESH_RATE = 60;
    private final ServerTapMain main;
    private final Logger log;
    private final ApiV1Initializer api;
    private final ServerSideEventsHandler sse;
    private final FileConfiguration bukkitConfig;
    private final BukkitScheduler scheduler;
    private final boolean sseEnabled;
    private final boolean reversePollingEnabled;

    public ReversePollingInitializer(ServerTapMain main, Logger log, ApiV1Initializer api, ServerSideEventsHandler see) {
        this.main = main;
        this.log = log;
        this.api = api;
        this.sse = see;
        this.bukkitConfig = main.getConfig();
        this.sseEnabled = bukkitConfig.getBoolean("sse.enabled", false);
        this.scheduler = Bukkit.getScheduler();
        this.reversePollingEnabled = bukkitConfig.getBoolean("reversePolling.enabled", false);

        initReversePolling();
    }

    private void initReversePolling() {
        if(!reversePollingEnabled)
            return;
        if(!sseEnabled) {
            log.info("[ServerTap] Error starting reverse polling: SSE IS NOT enabled on this server!");
        }

        setUpPollingFor("serverPolling", () -> sse.broadcast(Constants.UPDATE_SERVER_DATA_EVENT, api.getServerApi().getServer()));
        setUpPollingFor("worldsPolling", () -> sse.broadcast(Constants.UPDATE_WORLD_DATA_EVENT, api.getWorldApi().getWorlds()));
        setUpPollingFor("scoreboardPolling", () -> sse.broadcast(Constants.UPDATE_SCOREBOARD_DATA_EVENT, api.getServerApi().getScoreboard()));
        setUpPollingFor("advancementsPolling", () -> sse.broadcast(Constants.UPDATE_ADVANCEMENTS_DATA_EVENT, api.getAdvancementsApi().getAdvancements()));
    }

    private void setUpPollingFor(String name, Runnable callback) {
        boolean enabled = bukkitConfig.getBoolean(String.format("reversePolling.%s.enabled", name), false);
        int configRefreshRate = bukkitConfig.getInt(String.format("reversePolling.%s.refreshRate", name), 60);
        long refreshRate = (configRefreshRate > 0.1 ? configRefreshRate * 20L : DEFAULT_REFRESH_RATE);
        if(enabled)
            scheduler.runTaskTimerAsynchronously(
                    main,
                    callback,
                    0,
                    refreshRate
            );
    }
}
