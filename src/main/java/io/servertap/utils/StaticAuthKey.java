package io.servertap.utils;

import io.servertap.Constants;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticAuthKey implements ConfigurationSerializable {
    static {
        ConfigurationSerialization.registerClass(StaticAuthKey.class);
    }

    private String name;
    private String key;
    private List<String> allow;
    private List<String> deny;
    private String order;

    public StaticAuthKey(Map<String, Object> data) {
        this.name = data.get("name").toString();
        this.key = data.get("key").toString();

        this.allow = new ArrayList<>();
        this.deny = new ArrayList<>();

        for(String rule : (List<String>) data.getOrDefault("allow", this.allow)) {
            this.allow.add(convertToRegex(rule));
        }
        for(String rule : (List<String>) data.getOrDefault("deny", this.deny)) {
            this.deny.add(convertToRegex(rule));
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("name", this.name);
        result.put("key", this.key);
        result.put("allow", this.allow);
        result.put("deny", this.deny);

        return result;
    }

    private String convertToRegex(String pattern) {
        pattern = pattern.replaceAll("/\\*\\*", "/.*");
        pattern = pattern.replaceAll("/\\*", "/[^/]*");

        return String.format("^%s$", pattern);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getAllow() {
        return allow;
    }

    public void setAllow(List<String> allow) {
        this.allow = allow;
    }

    public List<String> getDeny() {
        return deny;
    }

    public void setDeny(List<String> deny) {
        this.deny = deny;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
