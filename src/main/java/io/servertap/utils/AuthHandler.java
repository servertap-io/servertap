package io.servertap.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

public class AuthHandler {

    private static final Logger log = Bukkit.getLogger();
    private ConfigurationSection config;

    public AuthHandler(ConfigurationSection config) {
        this.config = config;
        loadConfig();
    }

    private void loadConfig() {
        log.info(String.join(", ", this.config.getKeys(true)));
    }

    public void reload() {
        loadConfig();
    }

    public boolean checkAuth(String principal, String resource) {
        return false;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public void setConfig(ConfigurationSection config) {
        this.config = config;
    }
}
