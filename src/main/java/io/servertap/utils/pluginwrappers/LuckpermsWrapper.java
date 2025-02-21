package io.servertap.utils.pluginwrappers;

import io.servertap.ServerTapMain;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;


public class LuckpermsWrapper {
    private final ServerTapMain main;
    private final Logger log;
    private LuckPerms luckperms;

    public LuckpermsWrapper(ServerTapMain main, Logger logger) {
        this.main = main;
        this.log = logger;

        setupLuckperms();
    }

    public boolean isAvailable() {
        return luckperms != null;
    }

    private void setupLuckperms() {
        if (main.getServer().getPluginManager().getPlugin("Luckperms") == null) {
            log.info("[ServerTap] Luckperms not detected");
            return;
        }
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
            luckperms = api;
            log.info(String.format("[ServerTap] Hooked LuckPerms provider: %s", api.getPluginMetadata().getVersion()));
        }



    }


}
