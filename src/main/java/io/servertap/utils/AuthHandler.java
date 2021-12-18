package io.servertap.utils;

import io.servertap.Constants;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
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
            StaticAuthKey authKey = new StaticAuthKey((LinkedHashMap<String, Object>)s);
            authDatabase.put(authKey.getKey(), authKey);
        }
    }

    public boolean checkAuth(String key, String route) {
        StaticAuthKey authKey = authDatabase.get(key);
        if (authKey == null) return false;

        // default deny
        boolean result = false;

        switch (authKey.getOrder().toLowerCase()) {
            case Constants.AUTH_ALLOW_DENY:

                for (String allow : authKey.getAllow()) {
                    allow = "^" + allow.replaceAll("\\*", ".*") + "$";
                    if (route.matches(allow)) {
                        log.info(String.format("ROUTE MATCH FOUND: '%s' ~ '%s'", route, allow));
                        return true;
                    }
                }

                break;
            case Constants.AUTH_DENY_ALLOW:
                break;
            default:
                return false;
        }

        return false;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public void setConfig(ConfigurationSection config) {
        this.config = config;
    }
}
