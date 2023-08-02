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

        boolean serverPollingEnabled = bukkitConfig.getBoolean("reversePolling.serverPolling.enabled", false);
        boolean worldsPollingEnabled = bukkitConfig.getBoolean("reversePolling.worldsPolling.enabled", false);
        boolean scoreboardPollingEnabled = bukkitConfig.getBoolean("reversePolling.scoreboardPolling.enabled", false);
        boolean advancementsPollingEnabled = bukkitConfig.getBoolean("reversePolling.advancementsPolling.enabled", false);

        int serverRefreshRate = bukkitConfig.getInt("reversePolling.serverPolling.refreshRate", 60);
        int worldsRefreshRate = bukkitConfig.getInt("reversePolling.worldsPolling.refreshRate", 60);
        int scoreboardRefreshRate = bukkitConfig.getInt("reversePolling.scoreboardPolling.refreshRate", 60);
        int advancementsRefreshRate = bukkitConfig.getInt("reversePolling.advancementsPolling.refreshRate", 60);

        if(serverPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast(Constants.UPDATE_SERVER_DATA_EVENT, api.getServerApi().getServer()), 0, (advancementsRefreshRate > 0.1 ? serverRefreshRate * 20L : DEFAULT_REFRESH_RATE));
        if(worldsPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast(Constants.UPDATE_WORLD_DATA_EVENT, api.getWorldApi().getWorlds()), 0, (advancementsRefreshRate > 0.1 ? worldsRefreshRate * 20L : DEFAULT_REFRESH_RATE));
        if(scoreboardPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast(Constants.UPDATE_SCOREBOARD_DATA_EVENT, api.getServerApi().getScoreboard()), 0, (advancementsRefreshRate > 0.1 ? scoreboardRefreshRate * 20L : DEFAULT_REFRESH_RATE));
        if(advancementsPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast(Constants.UPDATE_ADVANCEMENTS_DATA_EVENT, api.getAdvancementsApi().getAdvancements()), 0, (advancementsRefreshRate > 0.1 ? advancementsRefreshRate * 20L : DEFAULT_REFRESH_RATE));
    }
}
