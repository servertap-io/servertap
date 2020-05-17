package io.servertap.api.v1;

import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.servertap.PluginEntrypoint;
import io.servertap.api.v1.models.Player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

public class PlayerApi {

    @OpenApi(
        path = "/v1/players",
        summary = "Gets all currently online players",
        tags = {"Player"},
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = Player.class, isArray = true))
        }
    )
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

    @OpenApi(
        path = "/v1/players/:player",
        method = HttpMethod.GET,
        summary = "Gets a specific online player by their username",
        tags = {"Player"},
        pathParams = {
            @OpenApiParam(name = "player", description = "Username of the player")
        },
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = Player.class))
        }
    )
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

    @OpenApi(
        path = "/v1/allPlayers",
        summary = "Gets all players that have ever joined the server ",
        tags = {"Player"},
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = io.servertap.api.v1.models.OfflinePlayer.class, isArray = true))
        }
    )
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
