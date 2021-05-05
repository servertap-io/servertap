package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;
import org.bukkit.OfflinePlayer;

public class Whitelist {
    @Expose
    private String uuid = null;

    @Expose
    private String name = null;

    public Whitelist offlinePlayer(OfflinePlayer player) {
        this.uuid = player.getUniqueId().toString();
        this.name = player.getName();
        return this;
    }

    public Whitelist uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * The Player's UUID
     *
     * @return uuid
     **/
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Whitelist name(String displayName) {
        this.name = displayName;
        return this;
    }

    /**
     * The Player's display name
     *
     * @return displayName
     **/
    public String getName() {
        return name;
    }

    public void setName(String displayName) {
        this.name = displayName;
    }

    public boolean equals(Whitelist whitelist) {
        return whitelist.getUuid().equals(this.uuid);
    }
}