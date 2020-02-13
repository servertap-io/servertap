package io.servertap.api.v1;

import io.javalin.http.Context;
import io.servertap.api.v1.models.Player;
import org.bukkit.Bukkit;

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

            players.add(p);
        }));

        ctx.json(players);
    }

}
