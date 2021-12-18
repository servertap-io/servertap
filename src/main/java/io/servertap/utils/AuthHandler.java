package io.servertap.utils;

import io.servertap.Constants;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Logger;

public class AuthHandler {

    enum CheckResult {
        ALLOW,
        DENY,
        AMBIGUOUS,
    }

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
        CheckResult result = CheckResult.AMBIGUOUS;
        CheckResult allowResult = CheckResult.AMBIGUOUS;
        CheckResult denyResult = CheckResult.AMBIGUOUS;

        for (String allow : authKey.getAllow()) {
            allow = "^" + allow.replaceAll("\\*", "[^/]*") + "$";
            if (route.matches(allow)) {
                log.info(String.format("ROUTE MATCH FOUND: '%s' ~ '%s'", route, allow));
                allowResult = CheckResult.ALLOW;
            }
        }

        for (String deny : authKey.getDeny()) {
            deny = "^" + deny.replaceAll("\\*", "[^/]*") + "$";
            if (route.matches(deny)) {
                log.info(String.format("ROUTE MATCH FOUND: '%s' ~ '%s'", route, deny));
                denyResult = CheckResult.DENY;
            }
        }

        if (authKey.getOrder().equalsIgnoreCase(Constants.AUTH_ALLOW_DENY)) {
            if (allowResult == CheckResult.AMBIGUOUS && denyResult == CheckResult.AMBIGUOUS) {
                return true;
            }

            if (allowResult == CheckResult.ALLOW && denyResult == CheckResult.AMBIGUOUS) {
                return true;
            }

            if (allowResult == CheckResult.AMBIGUOUS && denyResult == CheckResult.DENY) {
                return false;
            }

            if (allowResult == CheckResult.ALLOW && denyResult == CheckResult.DENY) {
                return false;
            }
        } else if (authKey.getOrder().equalsIgnoreCase(Constants.AUTH_DENY_ALLOW)) {
            if (allowResult == CheckResult.AMBIGUOUS && denyResult == CheckResult.AMBIGUOUS) {
                return false;
            }

            if (allowResult == CheckResult.ALLOW && denyResult == CheckResult.AMBIGUOUS) {
                return true;
            }

            if (allowResult == CheckResult.AMBIGUOUS && denyResult == CheckResult.DENY) {
                return false;
            }

            if (allowResult == CheckResult.ALLOW && denyResult == CheckResult.DENY) {
                return false;
            }
        }

        return false;
    }

    private CheckResult routeMatch(List<String> routes, String check) {
        CheckResult result = CheckResult.AMBIGUOUS;

        for (String route : routes) {
            route = "^" + route.replaceAll("\\*", "[^/]*") + "$";
            if (check.matches(route)) {
                log.info(String.format("ROUTE MATCH FOUND: '%s' ~ '%s'", check, route));
                return CheckResult.ALLOW;
            }
        }

        return result;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public void setConfig(ConfigurationSection config) {
        this.config = config;
    }
}
