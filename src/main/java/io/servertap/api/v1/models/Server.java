package io.servertap.api.v1.models;

import java.util.Set;

/**
 * A Bukkit/Spigot/Paper server
 */
public class Server {
    private String name = null;

    private String motd = null;

    private String version = null;

    private String bukkitVersion = null;

    private String tps = null;

    private ServerHealth health = null;

    private Set<ServerBan> bannedIps = null;

    private Set<ServerBan> bannedPlayers = null;

    private Set<Whitelist> whitelistedPlayers = null;

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
}
