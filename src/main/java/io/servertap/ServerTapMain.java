package io.servertap;

import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.websockets.ConsoleListener;
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

public class ServerTapMain extends JavaPlugin {

    public static ServerTapMain instance;
    private static final java.util.logging.Logger log = Bukkit.getLogger();
    private static WebServer app = null;

    Logger rootLogger = (Logger) LogManager.getRootLogger();

    public ArrayList<ConsoleLine> consoleBuffer = new ArrayList<>();
    public int maxConsoleBufferSize = 1000;
    public boolean authEnabled = true;

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

        authEnabled = bukkitConfig.getBoolean("useKeyAuth", true);

        // Warn about default auth key
        if (authEnabled && "change_me".equals(bukkitConfig.getString("key", "change_me"))) {
            log.warning("[ServerTap] AUTH KEY IS SET TO DEFAULT \"change_me\"");
            log.warning("[ServerTap] CHANGE THE key IN THE config.yml FILE");
            log.warning("[ServerTap] FAILURE TO CHANGE THE KEY MAY RESULT IN SERVER COMPROMISE");
        }

        maxConsoleBufferSize = bukkitConfig.getInt("websocketConsoleBuffer");
        rootLogger.addFilter(new ConsoleListener(this));

        // Instantiate the web server (which will now load using the plugin's class loader).
        if (app == null) {
            app = new WebServer(this, bukkitConfig, log);
        }

        // Don't create a new instance if the plugin is reloaded
        app.start(bukkitConfig.getInt("port", 4567));

        WebServerRoutes.addV1Routes(app);

        getServer().getPluginManager().registerEvents(new WebhookEventListener(this, bukkitConfig, log), this);

        getServer().getServicesManager().register(ServerTapWebserverService.class, new ServerTapWebserverServiceImpl(this), this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        // Release port so that /reload will work
        if (app != null) {
            app.stop();
        }
    }

    public WebServer getWebServer() {
        return app;
    }
}
