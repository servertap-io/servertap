package io.servertap;

import io.javalin.Javalin;
import io.javalin.community.ssl.SSLPlugin;
import io.javalin.openapi.OpenApiContact;
import io.javalin.openapi.Security;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.SecurityComponentConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.servertap.api.v1.*;
import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.websockets.ConsoleListener;
import io.servertap.api.v1.websockets.WebsocketHandler;
import io.servertap.metrics.Metrics;
import io.servertap.utils.EconomyWrapper;
import io.servertap.utils.GsonJsonMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.*;

public class PluginEntrypoint extends JavaPlugin {

    public static PluginEntrypoint instance;
    private static final java.util.logging.Logger log = Bukkit.getLogger();
    private EconomyWrapper economyWrapper;
    private static Javalin app = null;
    private List<Pattern> blockedPathRegexPatterns;

    public static final String SERVERTAP_KEY_HEADER = "key";
    public static final String SERVERTAP_KEY_COOKIE = "x-servertap-key";

    Logger rootLogger = (Logger) LogManager.getRootLogger();

    public ArrayList<ConsoleLine> consoleBuffer = new ArrayList<>();
    public int maxConsoleBufferSize = 1000;
    public boolean authEnabled = true;

    public PluginEntrypoint() {
        super();

        if (instance == null) {
            instance = this;
        }
    }

    public PluginEntrypoint(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description,
            @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        // Tell bStats what plugin this is
        Metrics metrics = new Metrics(this, 9492);

        // Initialize config file + set defaults
        saveDefaultConfig();
        FileConfiguration bukkitConfig = getConfig();

        // Initialize any economy integration (if one is installed)
        EconomyWrapper.getInstance().setupEconomy();

        this.authEnabled = bukkitConfig.getBoolean("useKeyAuth", true);

        // Warn about default auth key
        if (this.authEnabled) {
            if (bukkitConfig.getString("key", "change_me").equals("change_me")) {
                log.warning("[ServerTap] AUTH KEY IS SET TO DEFAULT \"change_me\"");
                log.warning("[ServerTap] CHANGE THE key IN THE config.yml FILE");
                log.warning("[ServerTap] FAILURE TO CHANGE THE KEY MAY RESULT IN SERVER COMPROMISE");
            }
        }

        // Convert blocked paths to regex patterns for faster matching
        blockedPathRegexPatterns = bukkitConfig.getStringList("blocked-paths").stream()
                // Replace Placeholders with regex patterns
                .map(path -> path
                        // Replace Config wildcards (*) with a Regex Wildcard
                        .replace("*", ".*")
                        // Replace {placeholders} with Not-A-Slash Regex
                        .replaceAll("\\{.*}", "[^/]*")
                        // Escape all / characters for Regex
                        .replace("/", "\\/"))
                .map(Pattern::compile)
                .collect(Collectors.toList());

        maxConsoleBufferSize = bukkitConfig.getInt("websocketConsoleBuffer");
        rootLogger.addFilter(new ConsoleListener(this));
        Bukkit.getScheduler().runTaskTimer(this, new Lag(), 100, 1);

        // Instantiate the web server (which will now load using the plugin's class
        // loader).
        if (app == null) {

            app = Javalin.create(config -> {
                config.jsonMapper(new GsonJsonMapper());

                config.http.defaultContentType = "application/json";
                config.showJavalinBanner = false;

                boolean tlsConfEnabled = bukkitConfig.getBoolean("tls.enabled", false);
                if (tlsConfEnabled) {
                    try {
                        String keystorePath = bukkitConfig.getString("tls.keystore", "keystore.jks");
                        String keystorePassword = bukkitConfig.getString("tls.keystorePassword", "");

                        final String fullKeystorePath = getDataFolder().getAbsolutePath() + File.separator
                                + keystorePath;

                        if (!Files.exists(Paths.get(fullKeystorePath))) {
                            log.warning(String.format("[ServerTap] TLS is enabled but %s doesn't exist. TLS disabled.",
                                    fullKeystorePath));
                        } else {
                            // register the SSL plugin
                            SSLPlugin plugin = new SSLPlugin(conf -> {
                                conf.keystoreFromPath(fullKeystorePath, keystorePassword);
                                conf.http2 = false;
                                conf.insecure = false;
                                conf.secure = true;
                                conf.securePort = bukkitConfig.getInt("port", 4567);
                                conf.sniHostCheck = bukkitConfig.getBoolean("tls.sni", false);
                            });
                            config.plugins.register(plugin);
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
                config.plugins.enableCors(cors -> cors.add(corsConfig -> {
                    if (corsOrigins.contains("*")) {
                        log.info("[ServerTap] Enabling CORS for *");
                        corsConfig.anyHost();
                    } else {
                        for (String origin : corsOrigins) {
                            log.info(String.format("[ServerTap] Enabling CORS for %s", origin));
                            corsConfig.allowHost(origin);
                        }
                    }
                }));

                // Create an accessManager to verify the path is a swagger call, or has the
                // correct authentication
                config.accessManager((handler, ctx, permittedRoles) -> {
                    String path = ctx.req().getPathInfo();

                    // If the path is blocked, return 403
                    if (blockedPathRegexPatterns.stream().anyMatch(pattern -> pattern.matcher(path).matches())) {
                        ctx.status(403).result("Forbidden");
                        return;
                    }

                    // If auth is not enabled just serve it all
                    if (!this.authEnabled) {
                        handler.handle(ctx);
                        return;
                    }

                    // At this point in the code, auth is enabled

                    // Add some paths that will always bypass auth
                    List<String> noAuthPathsList = new ArrayList<>();
                    noAuthPathsList.add("/swagger");
                    noAuthPathsList.add("/swagger-docs");
                    noAuthPathsList.add("/webjars");

                    // If the request path starts with any of the noAuthPathsList just allow it
                    for (String noAuthPath : noAuthPathsList) {
                        if (path.startsWith(noAuthPath)) {
                            handler.handle(ctx);
                            return;
                        }
                    }

                    // Auth is turned on, make sure there is a header called "key"
                    String authKey = bukkitConfig.getString("key", "change_me");
                    if (ctx.header(SERVERTAP_KEY_HEADER) != null && ctx.header(SERVERTAP_KEY_HEADER).equals(authKey)) {
                        handler.handle(ctx);
                        return;
                    }

                    // If the request is still not handled, check for a cookie (websockets use
                    // cookies for auth)
                    if (ctx.cookie(SERVERTAP_KEY_COOKIE) != null && ctx.cookie(SERVERTAP_KEY_COOKIE).equals(authKey)) {
                        handler.handle(ctx);
                        return;
                    }

                    // fall through, failsafe
                    ctx.status(401).result("Unauthorized key, reference the key existing in config.yml");
                });

                if (!bukkitConfig.getBoolean("disable-swagger", false)) {
                    config.plugins.register(new OpenApiPlugin(getOpenApiConfig()));

                    SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
                    swaggerConfiguration.setDocumentationPath("/swagger-docs");
                    config.plugins.register(new SwaggerPlugin(swaggerConfiguration));
                }
            });
        }

        // Don't create a new instance if the plugin is reloaded
        app.start(bukkitConfig.getInt("port", 4567));

        if (bukkitConfig.getBoolean("debug")) {
            app.before(ctx -> log.info(ctx.req().getPathInfo()));
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
                delete("server/whitelist", ServerApi::whitelistDelete);

                get("worlds", WorldApi::worldsGet);
                post("worlds/save", WorldApi::saveAllWorlds);
                get("worlds/download", WorldApi::downloadWorlds);
                get("worlds/{uuid}", WorldApi::worldGet);
                post("worlds/{uuid}/save", WorldApi::saveWorld);
                get("worlds/{uuid}/download", WorldApi::downloadWorld);

                get("scoreboard", ServerApi::scoreboardGet);
                get("scoreboard/{name}", ServerApi::objectiveGet);

                // Chat
                post("chat/broadcast", ServerApi::broadcastPost);
                post("chat/tell", ServerApi::tellPost);

                // Player routes
                get("players", PlayerApi::playersGet);
                get("players/all", PlayerApi::offlinePlayersGet);
                get("players/{uuid}", PlayerApi::playerGet);
                get("players/{playerUuid}/{worldUuid}/inventory", PlayerApi::getPlayerInv);

                // Economy routes
                post("economy/pay", EconomyApi::playerPay);
                post("economy/debit", EconomyApi::playerDebit);
                get("economy", EconomyApi::getEconomyPluginInformation);

                // Plugin routes
                get("plugins", PluginApi::listPlugins);
                post("plugins", PluginApi::installPlugin);

                // PAPI Routes
                post("placeholders/replace", PAPIApi::replacePlaceholders);

                // Websocket handler
                ws("ws/console", WebsocketHandler::events);

                // Advancement routes
                get("advancements", AdvancementsApi::getAdvancements);
            });
        });

        getServer().getPluginManager().registerEvents(new WebhookEventListener(this), this);
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        // Release port so that /reload will work
        if (app != null) {
            app.stop();
        }

    }

    private OpenApiPluginConfiguration getOpenApiConfig() {
        Security security = new Security(null);
        
        SecurityComponentConfiguration securityComponentConfiguration = new SecurityComponentConfiguration();
        securityComponentConfiguration.
        return new OpenApiPluginConfiguration()
                .withDocumentationPath("/swagger-docs")
                .withDefinitionConfiguration((version, definition) -> definition
                        .withOpenApiInfo((openApiInfo) -> {
                            openApiInfo.setTitle(this.getDescription().getName());
                            openApiInfo.setVersion(this.getDescription().getVersion());
                            openApiInfo.setDescription(this.getDescription().getDescription());
                            OpenApiContact contact = new OpenApiContact();
                            contact.setName("ServerTap Discord");
                            contact.setUrl("https://discord.gg/fefHbTFAkj");
                            openApiInfo.setContact(contact);
                            
                        })
                        .withSecurity());
    }
}
