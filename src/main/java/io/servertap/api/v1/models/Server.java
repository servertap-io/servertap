package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

import java.util.Set;

/**
 * A Bukkit/Spigot/Paper server
 */
public class Server {
    @Expose
    private String name = null;

    @Expose
    private String motd = null;

    @Expose
    private String version = null;

    @Expose
    private String bukkitVersion = null;

    @Expose
    private String tps = null;

    @Expose
    private ServerHealth health = null;

    @Expose
    private Set<ServerBan> bannedIps = null;

    @Expose
    private Set<ServerBan> bannedPlayers = null;

    @Expose
    private Set<Whitelist> whitelistedPlayers = null;

    @Expose
    private int maxPlayers = 0;

    @Expose
    private int onlinePlayers = 0;

    public Server name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The name of the server
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServerHealth getHealth() {
        return health;
    }

    public void setHealth(ServerHealth health) {
        this.health = health;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTps() {
        return tps;
    }

    public void setTps(String tps) {
        this.tps = tps;
    }

    public String getBukkitVersion() {
        return bukkitVersion;
    }

    public void setBukkitVersion(String bukkitVersion) {
        this.bukkitVersion = bukkitVersion;
    }

    public Set<ServerBan> getBannedIps() {
        return bannedIps;
    }

    public void setBannedIps(Set<ServerBan> bannedIps) {
        this.bannedIps = bannedIps;
    }

    public Set<ServerBan> getBannedPlayers() {
        return bannedPlayers;
    }

    public void setBannedPlayers(Set<ServerBan> bannedPlayers) {
        this.bannedPlayers = bannedPlayers;
    }

    public Set<Whitelist> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public void setWhitelistedPlayers(Set<Whitelist> whitelistPlayers) {
        this.whitelistedPlayers = whitelistPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }
}
