package io.servertap;

import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.commands.ServerTapCommand;
import io.servertap.metrics.Metrics;
import io.servertap.plugin.api.ServerTapWebserverService;
import io.servertap.plugin.api.ServerTapWebserverServiceImpl;
import io.servertap.services.RadomUrlSafeGenerator;
import io.servertap.utils.ConsoleListener;
import io.servertap.utils.LagDetector;
import io.servertap.utils.pluginwrappers.ExternalPluginWrapperRepo;
import io.servertap.webhooks.WebhookEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerTapMain extends JavaPlugin {

    private static final java.util.logging.Logger log = Bukkit.getLogger();
    private final Logger rootLogger = (Logger) LogManager.getRootLogger();
    private final List<ConsoleLine> consoleBuffer = new ArrayList<>();
    private ExternalPluginWrapperRepo externalPluginWrapperRepo;
    private WebhookEventListener webhookEventListener;
    private int maxConsoleBufferSize = 1000;
    private ConsoleListener consoleListener;
    public static ServerTapMain instance;
    private final LagDetector lagDetector;
    private final Server server;
    private WebServer app;

    public ServerTapMain() {
        super();
        instance = this;
        server = getServer();
        lagDetector = new LagDetector();
    }

    public ServerTapMain(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
        instance = this;
        server = getServer();
        lagDetector = new LagDetector();
    }

    @Override
    public void onEnable() {
        // Tell bStats what plugin this is
        new Metrics(this, 9492);

        // Initialize any external plugin integrations
        externalPluginWrapperRepo = new ExternalPluginWrapperRepo(this, log);

        // Start the TPS Counter with a 100 tick Delay every 1 tick
        Bukkit.getScheduler().runTaskTimer(this, lagDetector, 100, 1);

        // Initialize config file + set defaults
        saveDefaultConfig();

        FileConfiguration bukkitConfig = getConfig();
        maxConsoleBufferSize = bukkitConfig.getInt("websocketConsoleBuffer");
        consoleListener = new ConsoleListener(this);
        rootLogger.addFilter(consoleListener);

        setupWebServer(bukkitConfig);

        new ServerTapCommand(this);

        webhookEventListener = new WebhookEventListener(this, bukkitConfig, log, externalPluginWrapperRepo.getEconomyWrapper());
        server.getPluginManager().registerEvents(webhookEventListener, this);

        server.getServicesManager().register(ServerTapWebserverService.class, new ServerTapWebserverServiceImpl(this), this, ServicePriority.Normal);
    }

    private void setupWebServer(FileConfiguration bukkitConfig) {
        String webhookString = RadomUrlSafeGenerator.generateRandomURLSafeString(60);
        app = new WebServer(this, bukkitConfig, log, webhookString);
        app.start(bukkitConfig.getInt("port", 4567));
        WebServerRoutes.addV1Routes(this, log, lagDetector, app, consoleListener, externalPluginWrapperRepo, webhookString);
    }

    public void reload() {
        if (app != null) {
            app.stop();
        }
        log.info("[ServerTap] ServerTap reloading...");
        reloadConfig();
        FileConfiguration bukkitConfig = getConfig();
        maxConsoleBufferSize = bukkitConfig.getInt("websocketConsoleBuffer");

        externalPluginWrapperRepo = new ExternalPluginWrapperRepo(this, log);
        consoleListener.resetListeners();

        setupWebServer(bukkitConfig);

        webhookEventListener.loadWebhooksFromConfig(bukkitConfig);
        log.info("[ServerTap] ServerTap reloaded successfully!");
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        if (app != null) {
            app.stop();
        }
    }

    public int getMaxConsoleBufferSize() {
        return this.maxConsoleBufferSize;
    }

    public List<ConsoleLine> getConsoleBuffer() {
        return this.consoleBuffer;
    }

    public WebServer getWebServer() {
        return this.app;
    }
}
