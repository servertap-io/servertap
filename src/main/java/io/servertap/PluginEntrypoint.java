package io.servertap;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.servertap.api.v1.EconomyApi;
import io.servertap.api.v1.PAPIApi;
import io.servertap.api.v1.PlayerApi;
import io.servertap.api.v1.ServerApi;
import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.websockets.ConsoleListener;
import io.servertap.api.v1.websockets.WebsocketHandler;
import io.swagger.v3.oas.models.info.Info;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class PluginEntrypoint extends JavaPlugin {

    public static PluginEntrypoint instance;
    private static final java.util.logging.Logger log = Bukkit.getLogger();
    private static Economy econ = null;
    private static Javalin app = null;

    public static final String SERVERTAP_KEY_HEADER = "key";
    public static final String SERVERTAP_KEY_COOKIE = "x-servertap-key";

    Logger rootLogger = (Logger) LogManager.getRootLogger();

    public ArrayList<ConsoleLine> consoleBuffer = new ArrayList<>();
    public int maxConsoleBufferSize = 1000;

    public PluginEntrypoint() {
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    public void onEnable() {
        // Tell bStats what plugin this is
        Metrics metrics = new Metrics(this, 9492);

        saveDefaultConfig();
        FileConfiguration bukkitConfig = getConfig();
        setupEconomy();

        maxConsoleBufferSize = bukkitConfig.getInt("websocketConsoleBuffer");
        rootLogger.addFilter(new ConsoleListener(this));
        Bukkit.getScheduler().runTaskTimer(this, new Lag(), 100, 1);

        // Get the current class loader.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Temporarily set this thread's class loader to the plugin's class loader.
        // Replace JavalinTestPlugin.class with your own plugin's class.
        Thread.currentThread().setContextClassLoader(PluginEntrypoint.class.getClassLoader());

        // Instantiate the web server (which will now load using the plugin's class
        // loader).
        if (app == null) {

            app = Javalin.create(config -> {
                config.defaultContentType = "application/json";
                config.showJavalinBanner = false;

                boolean tlsConfEnabled = bukkitConfig.getBoolean("tls.enabled", false);
                if (tlsConfEnabled) {
                    try {
                        String keystorePath = bukkitConfig.getString("tls.keystore", "keystore.jks");
                        String keystorePassword = bukkitConfig.getString("tls.keystorePassword", "");

                        final String fullKeystorePath = getDataFolder().getAbsolutePath() + File.separator + keystorePath;

                        if (!Files.exists(Paths.get(fullKeystorePath))) {
                            log.warning(String.format("[ServerTap] TLS is enabled but %s doesn't exist. TLS disabled.", fullKeystorePath));
                        } else {
                            config.server(() -> {
                                Server server = new Server();
                                ServerConnector sslConnector = new ServerConnector(server, getSslContextFactory(fullKeystorePath, keystorePassword));
                                sslConnector.setPort(bukkitConfig.getInt("port", 4567));
                                server.setConnectors(new Connector[]{sslConnector});
                                return server;
                            });

                            log.info("[ServerTap] TLS is enabled.");
                        }
                    } catch (Exception e) {
                        log.severe("[ServerTap] Error while enabling TLS: " + e.getMessage());
                        log.warning("[ServerTap] TLS is not enabled.");
                    }
                } else {
                    log.warning("[ServerTap] TLS is not enabled.");
                }

                // unpack the list of strings into varargs
                List<String> corsOrigins = bukkitConfig.getStringList("corsOrigins");
                String[] originArray = new String[corsOrigins.size()];
                for (int i = 0; i < originArray.length; i++) {
                    log.info(String.format("[ServerTap] Enabling CORS for %s", corsOrigins.get(i)));
                    originArray[i] = corsOrigins.get(i);
                }
                config.enableCorsForOrigin(originArray);

                // Create an accessManager to verify the path is a swagger call, or has the correct authentication
                config.accessManager((handler, ctx, permittedRoles) -> {
                    String path = ctx.req.getPathInfo();
                    String[] noAuthPaths = new String[]{"/swagger", "/swagger-docs"};
                    List<String> noAuthPathsList = Arrays.asList(noAuthPaths);

                    // If the request is for an excluded path, or the user has auth turned off, just serve the req
                    if (noAuthPathsList.contains(path) || !bukkitConfig.getBoolean("useKeyAuth", false)) {
                        handler.handle(ctx);
                        return;
                    }

                    // Auth is turned on, make sure there is a header called "key"
                    String authKey = bukkitConfig.getString("key", "change_me");
                    if (ctx.header(SERVERTAP_KEY_HEADER) != null && ctx.header(SERVERTAP_KEY_HEADER).equals(authKey)) {
                        handler.handle(ctx);
                        return;
                    }

                    // If the request is still not handled, check for a cookie (websockets use cookies for auth)
                    if (ctx.cookie(SERVERTAP_KEY_COOKIE) != null && ctx.cookie(SERVERTAP_KEY_COOKIE).equals(authKey)) {
                        handler.handle(ctx);
                        return;
                    }

                    // fall through, failsafe
                    ctx.status(401).result("Unauthorized key, reference the key existing in config.yml");
                });

                config.registerPlugin(new OpenApiPlugin(getOpenApiOptions()));
            });

        }

        // Don't create a new instance if the plugin is reloaded
        app.start(bukkitConfig.getInt("port", 4567));

        if (bukkitConfig.getBoolean("debug")) {
            app.before(ctx -> log.info(ctx.req.getPathInfo()));
        }

        app.routes(() -> {
            // Routes for v1 of the API
            path(Constants.API_V1, () -> {
                // Pings
                get("ping", ServerApi::ping);

                // Server routes
                get("server", ServerApi::serverGet);
                post("server/exec", ServerApi::postCommand);
                get("server/ops", ServerApi::getOps);
                post("server/ops", ServerApi::opPlayer);
                delete("server/ops", ServerApi::deopPlayer);
                get("server/whitelist", ServerApi::whitelistGet);
                post("server/whitelist", ServerApi::whitelistPost);
                get("worlds", ServerApi::worldsGet);
                post("worlds/save", ServerApi::saveAllWorlds);
                get("worlds/:uuid", ServerApi::worldGet);
                post("worlds/:uuid/save", ServerApi::saveWorld);
                get("scoreboard", ServerApi::scoreboardGet);
                get("scoreboard/:name", ServerApi::objectiveGet);

                // Chat
                post("chat/broadcast", ServerApi::broadcastPost);
                post("chat/tell", ServerApi::tellPost);

                // Player routes
                get("players", PlayerApi::playersGet);
                get("players/all", PlayerApi::offlinePlayersGet);
                get("players/:uuid", PlayerApi::playerGet);
                get("players/:playerUuid/:worldUuid/inventory", PlayerApi::getPlayerInv);

                // Economy routes
                post("economy/pay", EconomyApi::playerPay);
                post("economy/debit", EconomyApi::playerDebit);
                get("economy", EconomyApi::getEconomyPluginInformation);

                // Plugin routes
                get("plugins", ServerApi::listPlugins);

                // PAPI Routes
                post("placeholders/replace", PAPIApi::replacePlaceholders);

                // Websocket handler
                ws("ws/console", WebsocketHandler::events);
            });
        });

        // Put the original class loader back where it was.
        Thread.currentThread().setContextClassLoader(classLoader);

        getServer().getPluginManager().registerEvents(new WebhookEventListener(this), this);
    }

    private static SslContextFactory getSslContextFactory(String keystorePath, String keystorePassword) {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);
        return sslContextFactory;
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        // Release port so that /reload will work
        if (app != null) {
            app.stop();
        }

    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    public static Economy getEconomy() {
        return econ;
    }

    private OpenApiOptions getOpenApiOptions() {
        Info applicationInfo = new Info()
                .title(this.getDescription().getName())
                .version(this.getDescription().getVersion())
                .description(this.getDescription().getDescription());
        return new OpenApiOptions(applicationInfo)
                .path("/swagger-docs")
                .activateAnnotationScanningFor("io.servertap.api.v1")
                .swagger(new SwaggerOptions("/swagger"));
    }

}
