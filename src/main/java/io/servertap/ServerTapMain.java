package io.servertap;

import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.websockets.ConsoleListener;
import io.servertap.commands.ServerTapCommand;
import io.servertap.metrics.Metrics;
import io.servertap.plugin.api.ServerTapWebserverService;
import io.servertap.plugin.api.ServerTapWebserverServiceImpl;
import io.servertap.utils.EconomyWrapper;
import io.servertap.utils.Lag;
import io.servertap.webhooks.WebhookEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
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

    public static ServerTapMain instance;
    private static final java.util.logging.Logger log = Bukkit.getLogger();
    private WebServer app;

    Logger rootLogger = (Logger) LogManager.getRootLogger();

    private final List<ConsoleLine> consoleBuffer = new ArrayList<>();
    private int maxConsoleBufferSize = 1000;
    private WebhookEventListener webhookEventListener;

    public ServerTapMain() {
        super();
        instance = this;
    }

    public ServerTapMain(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
        instance = this;
    }

    @Override
    public void onEnable() {
        // Tell bStats what plugin this is
        new Metrics(this, 9492);

        // Initialize any economy integration (if one is installed)
        new EconomyWrapper(log).setupEconomy();

        // Start the TPS Counter with a 100 tick Delay every 1 tick
        Bukkit.getScheduler().runTaskTimer(this, new Lag(), 100, 1);

        // Initialize config file + set defaults
        saveDefaultConfig();

        FileConfiguration bukkitConfig = getConfig();
        maxConsoleBufferSize = bukkitConfig.getInt("websocketConsoleBuffer");
        rootLogger.addFilter(new ConsoleListener(this));

        setupWebServer(bukkitConfig);

        new ServerTapCommand(this);

        webhookEventListener = new WebhookEventListener(this, bukkitConfig, log);
        getServer().getPluginManager().registerEvents(webhookEventListener, this);

        getServer().getServicesManager().register(ServerTapWebserverService.class, new ServerTapWebserverServiceImpl(this), this, ServicePriority.Normal);
    }

    private void setupWebServer(FileConfiguration bukkitConfig) {
        app = new WebServer(this, bukkitConfig, log);
        app.start(bukkitConfig.getInt("port", 4567));
        WebServerRoutes.addV1Routes(app);
    }

    public void reload() {
        if (app != null) {
            app.stop();
        }
        log.info("[ServerTap] ServerTap reloading...");
        reloadConfig();
        FileConfiguration bukkitConfig = getConfig();
        maxConsoleBufferSize = bukkitConfig.getInt("websocketConsoleBuffer");
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
