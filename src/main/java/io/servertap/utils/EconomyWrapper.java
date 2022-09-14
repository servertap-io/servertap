package io.servertap.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * We can't simply reference Economy in PluginEntrypoint due to OpenApi annotation doing reflection, which can fail at
 * runtime due to the `scope` of the Vault API being `provided`.
 *
 * More details here: https://github.com/phybros/servertap/issues/175
 */
public class EconomyWrapper {

    private static final java.util.logging.Logger log = Bukkit.getLogger();

    private static EconomyWrapper instance;
    private static Economy economy;

    public static EconomyWrapper getInstance() {
        if (instance == null) {
            instance = new EconomyWrapper();
        }

        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public void setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            log.info("[ServerTap] No Vault plugin detected");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            log.info("[ServerTap] No Economy providers detected");
            return;
        }

        log.info(String.format("[ServerTap] Hooked economy provider: %s", rsp.getProvider().getName()));
        economy = rsp.getProvider();
    }
}
