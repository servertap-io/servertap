package io.servertap.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

public class AuthHandler {
    private static final Logger log = Bukkit.getLogger();
    private ConfigurationSection config;

    private HashMap<String, StaticAuthKey> authDatabase;

    public AuthHandler(ConfigurationSection config) {
        this.authDatabase = new HashMap<>();
        this.config = config;
        loadConfig();
    }

    private void loadConfig() {
        log.info(String.join(", ", this.config.getKeys(true)));

        List<?> c = this.getConfig().getList("static");

        assert c != null;
        for (Object s : c) {
            StaticAuthKey authKey = new StaticAuthKey((LinkedHashMap<String, Object>) s);
            authDatabase.put(authKey.getKey(), authKey);
        }
    }

    public boolean checkAuth(String key, String route) {
        StaticAuthKey authKey = authDatabase.get(key);
        if (authKey == null) return false;

        // default deny
        boolean allowed = false;

        for (String allow : authKey.getAllow()) {
            if (route.matches(allow)) {
                log.info(String.format("ROUTE MATCH FOUND: '%s' ~ '%s'", route, allow));
                allowed = true;
            }
        }

        for (String deny : authKey.getDeny()) {
            if (route.matches(deny)) {
                log.info(String.format("ROUTE MATCH FOUND: '%s' ~ '%s'", route, deny));
                allowed = false;
            }
        }

        return allowed;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public void setConfig(ConfigurationSection config) {
        this.config = config;
    }
}
