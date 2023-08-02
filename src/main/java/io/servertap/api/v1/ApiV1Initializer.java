package io.servertap.api.v1;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.serverSideEvents.ReversePollingInitializer;
import io.servertap.api.v1.serverSideEvents.ServerSideEventListener;
import io.servertap.api.v1.serverSideEvents.ServerSideEventsHandler;
import io.servertap.custom.events.BanListUpdatedAsyncEvent;
import io.servertap.custom.events.IpBanListUpdatedAsyncEvent;
import io.servertap.custom.events.OperatorListUpdatedAsyncEvent;
import io.servertap.custom.events.WhitelistUpdatedAsyncEvent;
import io.servertap.utils.ConsoleListener;
import io.servertap.api.v1.websockets.WebsocketHandler;
import io.servertap.utils.FileWatcher;
import io.servertap.utils.LagDetector;
import io.servertap.utils.pluginwrappers.ExternalPluginWrapperRepo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public class ApiV1Initializer {
    private final ReversePollingInitializer reversePollingInitializer;
    private final ServerSideEventsHandler serverSideEventsHandler;
    private final ServerSideEventListener serverSideEventListener;
    private final WebsocketHandler websocketHandler;
    private final AdvancementsApi advancementsApi;
    private final FileWatcher fileWatcher;

    private final EconomyApi economyApi;
    private final PluginApi pluginApi;
    private final ServerApi serverApi;
    private final PlayerApi playerApi;
    private final WorldApi worldApi;
    private final PAPIApi papiApi;

    public ApiV1Initializer(ServerTapMain main, Logger log, LagDetector lagDetector, ConsoleListener consoleListener,
                            ExternalPluginWrapperRepo externalPluginWrapperRepo) {
        this.serverSideEventsHandler = new ServerSideEventsHandler();
        this.reversePollingInitializer = new ReversePollingInitializer(main, this, serverSideEventsHandler);
        this.serverSideEventListener = new ServerSideEventListener(main, log, this, externalPluginWrapperRepo.getEconomyWrapper(), serverSideEventsHandler);
        this.websocketHandler = new WebsocketHandler(main, log, consoleListener);
        this.advancementsApi = new AdvancementsApi();
        this.fileWatcher = new FileWatcher(main, log);
        this.economyApi = new EconomyApi(externalPluginWrapperRepo.getEconomyWrapper());
        this.pluginApi = new PluginApi(main, log);
        this.serverApi = new ServerApi(main, log, lagDetector, externalPluginWrapperRepo.getEconomyWrapper());
        this.playerApi = new PlayerApi(log, externalPluginWrapperRepo.getEconomyWrapper());
        this.worldApi = new WorldApi(main, log);
        this.papiApi = new PAPIApi();

        // Setup File Watchers
        PluginManager pm = Bukkit.getPluginManager();
        fileWatcher.watch("ops.json", "../", () -> pm.callEvent(new OperatorListUpdatedAsyncEvent()));
        fileWatcher.watch("whitelist.json", "../", () -> pm.callEvent(new WhitelistUpdatedAsyncEvent()));
        fileWatcher.watch("banned-players.json", "../", () -> pm.callEvent(new BanListUpdatedAsyncEvent()));
        fileWatcher.watch("banned-ips.json", "../", () -> pm.callEvent(new IpBanListUpdatedAsyncEvent()));
        fileWatcher.start();
    }

    public ServerSideEventsHandler getServerSideEventsHandler() {
        return serverSideEventsHandler;
    }

    public WebsocketHandler getWebsocketHandler() {
        return websocketHandler;
    }

    public AdvancementsApi getAdvancementsApi() {
        return advancementsApi;
    }

    public EconomyApi getEconomyApi() {
        return economyApi;
    }

    public PluginApi getPluginApi() {
        return pluginApi;
    }

    public ServerApi getServerApi() {
        return serverApi;
    }

    public PlayerApi getPlayerApi() {
        return playerApi;
    }

    public WorldApi getWorldApi() {
        return worldApi;
    }

    public PAPIApi getPapiApi() {
        return papiApi;
    }
}
