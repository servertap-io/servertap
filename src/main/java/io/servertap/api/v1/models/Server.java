package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

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
    private ArrayList<ServerBan> bannedIps = null;

    @Expose
    private ArrayList<ServerBan> bannedPlayers = null;

    @Expose
    private ArrayList<Whitelist> whitelistedPlayers = null;

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

    public ArrayList<ServerBan> getBannedIps() {
        return bannedIps;
    }

    public void setBannedIps(ArrayList<ServerBan> bannedIps) {
        this.bannedIps = bannedIps;
    }

    public ArrayList<ServerBan> getBannedPlayers() {
        return bannedPlayers;
    }

    public void setBannedPlayers(ArrayList<ServerBan> bannedPlayers) {
        this.bannedPlayers = bannedPlayers;
    }

    public ArrayList<Whitelist> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public void setWhitelistedPlayers(ArrayList<Whitelist> whitelistPlayers) {
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

    private static ArrayList<Whitelist> getWhitelist(org.bukkit.Server bukkitServer) {
        ArrayList<Whitelist> whitelist = new ArrayList<>();
        bukkitServer.getWhitelistedPlayers().forEach((OfflinePlayer player) -> whitelist.add(new Whitelist().offlinePlayer(player)));
        return whitelist;
    }

    public static Server fromBukkitServer(org.bukkit.Server bukkitServer) {
        Server server = new Server();
        server.setName(bukkitServer.getName());
        server.setMotd(bukkitServer.getMotd());
        server.setVersion(bukkitServer.getVersion());
        server.setBukkitVersion(bukkitServer.getBukkitVersion());
        server.setWhitelistedPlayers(getWhitelist(bukkitServer));
        server.setMaxPlayers(bukkitServer.getMaxPlayers());
        server.setOnlinePlayers(bukkitServer.getOnlinePlayers().size());

        // Get the list of IP bans
        ArrayList<ServerBan> bannedIps = new ArrayList<>();
        bukkitServer.getBanList(BanList.Type.IP).getBanEntries().forEach(banEntry -> {
            ServerBan ban = new ServerBan();

            ban.setSource(banEntry.getSource());
            ban.setExpiration(banEntry.getExpiration());
            ban.setReason(ban.getReason());
            ban.setTarget(banEntry.getTarget());

            bannedIps.add(ban);
        });
        server.setBannedIps(bannedIps);

        // Get the list of player bans
        ArrayList<ServerBan> bannedPlayers = new ArrayList<>();
        bukkitServer.getBanList(BanList.Type.NAME).getBanEntries().forEach(banEntry -> {
            ServerBan ban = new ServerBan();

            ban.setSource(banEntry.getSource());
            ban.setExpiration(banEntry.getExpiration());
            ban.setReason(ban.getReason());
            ban.setTarget(banEntry.getTarget());

            bannedPlayers.add(ban);
        });
        server.setBannedPlayers(bannedPlayers);
        return server;
    }
}
