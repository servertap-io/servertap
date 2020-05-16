package io.servertap.api.v1;

import io.javalin.http.Context;
import io.servertap.PluginEntrypoint;
import io.servertap.api.v1.models.Player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

public class PlayerApi {

    public static void playersGet(Context ctx) {
        ArrayList<Player> players = new ArrayList<>();

        Bukkit.getOnlinePlayers().forEach((player -> {
            Player p = new Player();
            p.setUuid(player.getUniqueId().toString());
            p.setDisplayName(player.getDisplayName());

            p.setAddress(player.getAddress().getHostName());
            p.setPort(player.getAddress().getPort());

            p.setExhaustion(player.getExhaustion());
            p.setExp(player.getExp());

            p.setWhitelisted(player.isWhitelisted());
            p.setBanned(player.isBanned());
            p.setOp(player.isOp());

            if (PluginEntrypoint.getEconomy() != null) {
                p.setBalance(PluginEntrypoint.getEconomy().getBalance(player));
            }

            players.add(p);
        }));

        ctx.json(players);
    }

    public static void playerGet(Context ctx) {
        Player p = new Player();

        org.bukkit.entity.Player player = Bukkit.getPlayer(ctx.pathParam("player"));

        if (PluginEntrypoint.getEconomy() != null) {
            p.setBalance(PluginEntrypoint.getEconomy().getBalance(player));
        }

        p.setUuid(player.getUniqueId().toString());
        p.setDisplayName(player.getDisplayName());

        p.setAddress(player.getAddress().getHostName());
        p.setPort(player.getAddress().getPort());

        p.setExhaustion(player.getExhaustion());
        p.setExp(player.getExp());

        p.setWhitelisted(player.isWhitelisted());
        p.setBanned(player.isBanned());
        p.setOp(player.isOp());

        ctx.json(p);
    }

    public static void offlinePlayersGet(Context ctx) {

        ArrayList<io.servertap.api.v1.models.OfflinePlayer> players = new ArrayList<>();

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

        for (int i = 0; i < offlinePlayers.length; i++) {
            io.servertap.api.v1.models.OfflinePlayer p = new io.servertap.api.v1.models.OfflinePlayer();
            OfflinePlayer player = offlinePlayers[i];

            p.setDisplayName(player.getName());
            p.setUuid(player.getUniqueId().toString());
            p.setWhitelisted(player.isWhitelisted());
            p.setBanned(player.isBanned());
            p.setOp(player.isOp());

            if(PluginEntrypoint.getEconomy() != null){
                p.setBalance(PluginEntrypoint.getEconomy().getBalance(offlinePlayers[i]));
            }

            players.add(p);

        }

        ctx.json(players);
    }

}
