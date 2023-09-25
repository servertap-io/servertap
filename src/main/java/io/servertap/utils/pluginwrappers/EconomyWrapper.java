package io.servertap.utils.pluginwrappers;

import io.servertap.ServerTapMain;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

/**
 * We can't simply reference Economy in ServerTapMain due to OpenApi annotation doing reflection, which can fail at
 * runtime due to the `scope` of the Vault API being `provided`.<br>
 * <br>
 * More details here: <a href="https://github.com/servertap-io/servertap/issues/175">https://github.com/servertap-io/servertap/issues/175</a>
 */
public class EconomyWrapper {

    private final ServerTapMain main;
    private final Logger log;
    private Economy economy;

    public EconomyWrapper(ServerTapMain main, Logger logger) {
        this.main = main;
        this.log = logger;
        
        setupEconomy();
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public double getPlayerBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount);
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount);
    }

    private void setupEconomy() {
        if (main.getServer().getPluginManager().getPlugin("Vault") == null) {
            log.info("[ServerTap] No Vault plugin detected");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            log.info("[ServerTap] No Economy providers detected");
            return;
        }

        log.info(String.format("[ServerTap] Hooked economy provider: %s", rsp.getProvider().getName()));
        economy = rsp.getProvider();
    }
}
