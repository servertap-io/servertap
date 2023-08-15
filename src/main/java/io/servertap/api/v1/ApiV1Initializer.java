package io.servertap.api.v1;

import io.servertap.ServerTapMain;
import io.servertap.utils.ConsoleListener;
import io.servertap.api.v1.websockets.WebsocketHandler;
import io.servertap.utils.LagDetector;
import io.servertap.utils.pluginwrappers.ExternalPluginWrapperRepo;

import java.util.logging.Logger;

public class ApiV1Initializer {
    private final WebsocketHandler websocketHandler;
    private final AdvancementsApi advancementsApi;
    private final EconomyApi economyApi;
    private final PluginApi pluginApi;
    private final ServerApi serverApi;
    private final PlayerApi playerApi;
    private final WorldApi worldApi;
    private final PAPIApi papiApi;

    public ApiV1Initializer(ServerTapMain main, Logger log, LagDetector lagDetector, ConsoleListener consoleListener,
                            ExternalPluginWrapperRepo externalPluginWrapperRepo) {
        this.websocketHandler = new WebsocketHandler(main, log, consoleListener);
        this.advancementsApi = new AdvancementsApi();
        this.economyApi = new EconomyApi(externalPluginWrapperRepo.getEconomyWrapper());
        this.pluginApi = new PluginApi(main, log);
        this.serverApi = new ServerApi(main, log, lagDetector, externalPluginWrapperRepo.getEconomyWrapper());
        this.playerApi = new PlayerApi(log, externalPluginWrapperRepo.getEconomyWrapper());
        this.worldApi = new WorldApi(main, log);
        this.papiApi = new PAPIApi();
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
