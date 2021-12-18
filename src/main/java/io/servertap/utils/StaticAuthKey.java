package io.servertap.utils;

import io.servertap.Constants;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
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

        List<String> defaultDeny = new ArrayList<>();
        List<String> defaultAllow = new ArrayList<>();
        defaultAllow.add("/v1/*");

        this.allow = (List<String>) data.getOrDefault("allow", defaultAllow);
        this.deny = (List<String>) data.getOrDefault("deny", defaultDeny);

        this.order = data.getOrDefault("order", Constants.AUTH_DENY_ALLOW).toString();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("name", this.name);
        result.put("key", this.key);
        result.put("allow", this.allow);
        result.put("deny", this.deny);
        result.put("order", this.order);

        return result;
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
