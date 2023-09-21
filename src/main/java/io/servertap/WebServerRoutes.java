package io.servertap;

import io.javalin.http.Handler;
import io.javalin.websocket.WsConfig;
import io.servertap.api.v1.*;
import io.servertap.utils.ConsoleListener;
import io.servertap.utils.LagDetector;
import io.servertap.utils.pluginwrappers.ExternalPluginWrapperRepo;

import static io.servertap.Constants.*;

import java.util.function.Consumer;
import java.util.logging.Logger;

public final class WebServerRoutes {

    private WebServerRoutes() {}

    public static void addV1Routes(ServerTapMain main, Logger log, LagDetector lagDetector, WebServer webServer,
                                   ConsoleListener consoleListener, ExternalPluginWrapperRepo externalPluginWrapperRepo) {
        PrefixedRouteBuilder pr = new PrefixedRouteBuilder(API_V1, webServer);

        ApiV1Initializer api = new ApiV1Initializer(main, log, lagDetector, consoleListener, externalPluginWrapperRepo);

        pr.post("login",api.getAuthApi()::login);

        pr.get("ping", api.getServerApi()::ping);

        // Server routes
        pr.get("server", api.getServerApi()::serverGet);
        pr.post("server/exec", api.getServerApi()::postCommand);
        pr.get("server/ops", api.getServerApi()::getOps);
        pr.post("server/ops", api.getServerApi()::opPlayer);
        pr.delete("server/ops", api.getServerApi()::deopPlayer);
        pr.get("server/whitelist", api.getServerApi()::whitelistGet);
        pr.post("server/whitelist", api.getServerApi()::whitelistPost);
        pr.delete("server/whitelist", api.getServerApi()::whitelistDelete);

        pr.get("worlds", api.getWorldApi()::worldsGet);
        pr.post("worlds/save", api.getWorldApi()::saveAllWorlds);
        pr.get("worlds/download", api.getWorldApi()::downloadWorlds);
        pr.get("worlds/{uuid}", api.getWorldApi()::worldGet);
        pr.post("worlds/{uuid}/save", api.getWorldApi()::saveWorld);
        pr.get("worlds/{uuid}/download", api.getWorldApi()::downloadWorld);

        pr.get("scoreboard", api.getServerApi()::scoreboardGet);
        pr.get("scoreboard/{name}", api.getServerApi()::objectiveGet);

        // Chat
        pr.post("chat/broadcast", api.getServerApi()::broadcastPost);
        pr.post("chat/tell", api.getServerApi()::tellPost);

        // Player routes
        pr.get("players", api.getPlayerApi()::playersGet);
        pr.get("players/all", api.getPlayerApi()::offlinePlayersGet);
        pr.get("players/{uuid}", api.getPlayerApi()::playerGet);
        pr.get("players/{playerUuid}/{worldUuid}/inventory", api.getPlayerApi()::getPlayerInv);

        // Economy routes
        pr.post("economy/pay", api.getEconomyApi()::playerPay);
        pr.post("economy/debit", api.getEconomyApi()::playerDebit);
        pr.get("economy", api.getEconomyApi()::getEconomyPluginInformation);

        // Plugin routes
        pr.get("plugins", api.getPluginApi()::listPlugins);
        pr.post("plugins", api.getPluginApi()::installPlugin);

        // PAPI Routes
        pr.post("placeholders/replace", api.getPapiApi()::replacePlaceholders);

        // Websocket handler
        pr.ws("ws/console", api.getWebsocketHandler()::events);

        // Advancement routes
        pr.get("advancements", api.getAdvancementsApi()::getAdvancements);
    }

    private static class PrefixedRouteBuilder {
        private final String prefix;
        private final WebServer webServer;

        public PrefixedRouteBuilder(String prefix, WebServer webServer) {
            this.prefix = prefix;
            this.webServer = webServer;
        }

        public void get(String route, Handler handler) {
            webServer.get(prefix + "/" + route, handler);
        }

        public void post(String route, Handler handler) {
            webServer.post(prefix + "/" + route, handler);
        }

        public void put(String route, Handler handler) {
            webServer.put(prefix + "/" + route, handler);
        }

        public void delete(String route, Handler handler) {
            webServer.delete(prefix + "/" + route, handler);
        }

        public void ws(String route, Consumer<WsConfig> wsConfig) {
            webServer.ws(prefix + "/" + route, wsConfig);
        }
    }
}
