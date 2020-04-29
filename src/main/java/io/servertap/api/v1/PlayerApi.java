package io.servertap.api.v1;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.servertap.PluginEntrypoint;
import io.servertap.api.v1.models.Player;
import net.milkbowl.vault.economy.Economy;
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
            double curbalance = PluginEntrypoint.getEconomy().getBalance(player.getName());
            p.setBalance(curbalance);
            players.add(p);
        }));

        ctx.json(players);
    }

    public static void playerGet(Context ctx) {
        Player p = new Player();

        org.bukkit.entity.Player player= Bukkit.getPlayer(ctx.pathParam("player"));


        double curbalance = PluginEntrypoint.getEconomy().getBalance(player.getName());
        p.setBalance(curbalance);
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

        for (int i = 0; i <offlinePlayers.length ; i++) {
            io.servertap.api.v1.models.OfflinePlayer p = new io.servertap.api.v1.models.OfflinePlayer();
            OfflinePlayer player=offlinePlayers[i];

            p.setDisplayName(player.getName());
            p.setUuid(player.getUniqueId().toString());
            p.setWhitelisted(player.isWhitelisted());
            p.setBanned(player.isBanned());
            p.setOp(player.isOp());

            double curbalance = PluginEntrypoint.getEconomy().getBalance(player.getName());

            p.setBalance(curbalance);
            players.add(p);

        }

        ctx.json(players);
    }




    public static void playerPay(Context ctx) {


        io.servertap.api.v1.models.OfflinePlayer p = new io.servertap.api.v1.models.OfflinePlayer();
        OfflinePlayer player = Bukkit.getOfflinePlayer(ctx.pathParam("uuid"));

        double curbalance = PluginEntrypoint.getEconomy().getBalance(player.getName());

        PluginEntrypoint.getEconomy().depositPlayer(player, Double.parseDouble(ctx.pathParam("value")));

        p.setBalance(curbalance+Double.parseDouble(ctx.pathParam("value")));
        p.setUuid(player.getUniqueId().toString());
        p.setDisplayName(player.getName());
        p.setWhitelisted(player.isWhitelisted());
        p.setBanned(player.isBanned());
        p.setOp(player.isOp());


        ctx.json(p);
    }
}
