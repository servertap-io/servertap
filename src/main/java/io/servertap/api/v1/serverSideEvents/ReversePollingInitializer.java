package io.servertap.api.v1.serverSideEvents;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

public class ReversePollingInitializer {
    private final int DEFAULT_REFRESH_RATE = 60;
    private final ServerTapMain main;
    private final ApiV1Initializer api;
    private final ServerSideEventsHandler sse;
    private final FileConfiguration bukkitConfig;
    private final BukkitScheduler scheduler;
    private final boolean reversePollingEnabled;
    public ReversePollingInitializer(ServerTapMain main, ApiV1Initializer api, ServerSideEventsHandler see) {
        this.main = main;
        this.api = api;
        this.sse = see;
        this.bukkitConfig = main.getConfig();
        this.scheduler = Bukkit.getScheduler();
        this.reversePollingEnabled = bukkitConfig.getBoolean("reversePolling.enabled", false);

        initReversePolling();
    }

    private void initReversePolling() {
        if(!reversePollingEnabled)
            return;

        boolean serverPollingEnabled = bukkitConfig.getBoolean("reversePolling.serverPolling.enabled", false);
        boolean worldsPollingEnabled = bukkitConfig.getBoolean("reversePolling.worldsPolling.enabled", false);
        boolean scoreboardPollingEnabled = bukkitConfig.getBoolean("reversePolling.scoreboardPolling.enabled", false);
        boolean advancementsPollingEnabled = bukkitConfig.getBoolean("reversePolling.advancementsPolling.enabled", false);

        int serverRefreshRate = bukkitConfig.getInt("reversePolling.serverPolling.refreshRate", 60);
        int worldsRefreshRate = bukkitConfig.getInt("reversePolling.worldsPolling.refreshRate", 60);
        int scoreboardRefreshRate = bukkitConfig.getInt("reversePolling.scoreboardPolling.refreshRate", 60);
        int advancementsRefreshRate = bukkitConfig.getInt("reversePolling.advancementsPolling.refreshRate", 60);

        if(serverPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast("updateServerData", api.getServerApi().getServer()), 0, (advancementsRefreshRate > 0.1 ? serverRefreshRate * 20L : DEFAULT_REFRESH_RATE));
        if(worldsPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast("updateWorldsData", api.getWorldApi().getWorlds()), 0, (advancementsRefreshRate > 0.1 ? worldsRefreshRate * 20L : DEFAULT_REFRESH_RATE));
        if(scoreboardPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast("updateScoreboardData", api.getServerApi().getScoreboard()), 0, (advancementsRefreshRate > 0.1 ? scoreboardRefreshRate * 20L : DEFAULT_REFRESH_RATE));
        if(advancementsPollingEnabled) scheduler.runTaskTimerAsynchronously(main, () -> sse.broadcast("updateAdvancementsData", api.getAdvancementsApi().getAdvancements()), 0, (advancementsRefreshRate > 0.1 ? advancementsRefreshRate * 20L : DEFAULT_REFRESH_RATE));
    }
}
