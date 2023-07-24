package io.servertap.api.v1;

import io.servertap.ServerTapMain;
import io.servertap.utils.ConsoleListener;
import io.servertap.api.v1.websockets.WebsocketHandler;

import java.util.logging.Logger;

public class ServerTapApiV1 {
    private final WebsocketHandler websocketHandler;
    private final AdvancementsApi advancementsApi;
    private final EconomyApi economyApi;
    private final PluginApi pluginApi;
    private final ServerApi serverApi;
    private final PlayerApi playerApi;
    private final WorldApi worldApi;
    private final PAPIApi papiApi;

    public ServerTapApiV1(ServerTapMain main, Logger log, ConsoleListener consoleListener) {
        this.websocketHandler = new WebsocketHandler(main, log, consoleListener);
        this.advancementsApi = new AdvancementsApi();
        this.economyApi = new EconomyApi();
        this.pluginApi = new PluginApi(main, log);
        this.serverApi = new ServerApi(log);
        this.playerApi = new PlayerApi(log);
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
