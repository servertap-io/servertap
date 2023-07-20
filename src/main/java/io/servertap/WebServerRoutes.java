package io.servertap;

import io.javalin.http.Handler;
import io.javalin.websocket.WsConfig;
import io.servertap.api.v1.*;
import io.servertap.api.v1.websockets.WebsocketHandler;

import java.util.function.Consumer;

import static io.servertap.Constants.*;

public final class WebServerRoutes {

    private WebServerRoutes() {}

    public static void addV1Routes(WebServer webServer) {
        PrefixedRouteBuilder pr = new PrefixedRouteBuilder(API_V1, webServer);

        pr.get("ping", ServerApi::ping);

        // Server routes
        pr.get("server", ServerApi::serverGet);
        pr.post("server/exec", ServerApi::postCommand);
        pr.get("server/ops", ServerApi::getOps);
        pr.post("server/ops", ServerApi::opPlayer);
        pr.delete("server/ops", ServerApi::deopPlayer);
        pr.get("server/whitelist", ServerApi::whitelistGet);
        pr.post("server/whitelist", ServerApi::whitelistPost);
        pr.delete("server/whitelist", ServerApi::whitelistDelete);

        pr.get("worlds", WorldApi::worldsGet);
        pr.post("worlds/save", WorldApi::saveAllWorlds);
        pr.get("worlds/download", WorldApi::downloadWorlds);
        pr.get("worlds/{uuid}", WorldApi::worldGet);
        pr.post("worlds/{uuid}/save", WorldApi::saveWorld);
        pr.get("worlds/{uuid}/download", WorldApi::downloadWorld);

        pr.get("scoreboard", ServerApi::scoreboardGet);
        pr.get("scoreboard/{name}", ServerApi::objectiveGet);

        // Chat
        pr.post("chat/broadcast", ServerApi::broadcastPost);
        pr.post("chat/tell", ServerApi::tellPost);

        // Player routes
        pr.get("players", PlayerApi::playersGet);
        pr.get("players/all", PlayerApi::offlinePlayersGet);
        pr.get("players/{uuid}", PlayerApi::playerGet);
        pr.get("players/{playerUuid}/{worldUuid}/inventory", PlayerApi::getPlayerInv);

        // Economy routes
        pr.post("economy/pay", EconomyApi::playerPay);
        pr.post("economy/debit", EconomyApi::playerDebit);
        pr.get("economy", EconomyApi::getEconomyPluginInformation);

        // Plugin routes
        pr.get("plugins", PluginApi::listPlugins);
        pr.post("plugins", PluginApi::installPlugin);

        // PAPI Routes
        pr.post("placeholders/replace", PAPIApi::replacePlaceholders);

        // Websocket handler
        pr.ws("ws/console", WebsocketHandler::events);

        // Advancement routes
        pr.get("advancements", AdvancementsApi::getAdvancements);
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
