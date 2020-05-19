package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.servertap.Constants;
import io.servertap.PluginEntrypoint;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.Player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.InventoryHolder;

import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTListCompound;
import de.tr7zw.nbtapi.data.PlayerData;
import de.tr7zw.nbtapi.plugin.NBTAPI;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

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

        if (ctx.pathParam("uuid").isEmpty()) {
            throw new BadRequestResponse(Constants.PLAYER_UUID_MISSING);
        }

        org.bukkit.entity.Player player = Bukkit.getPlayer(UUID.fromString(ctx.pathParam("uuid")));

        if (player == null) {
            throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);
        }

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

            if (PluginEntrypoint.getEconomy() != null) {
                p.setBalance(PluginEntrypoint.getEconomy().getBalance(offlinePlayers[i]));
            }

            players.add(p);

        }

        ctx.json(players);
    }

    public static void getPlayerInv(Context ctx) {
        if (ctx.pathParam("uuid") == null || ctx.pathParam("world") == null) {
            // TODO: Move to Constants
            throw new InternalServerErrorResponse(Constants.PLAYER_MISSING_PARAMS);
        }
        ArrayList<ItemStack> inv = new ArrayList<ItemStack>();
        org.bukkit.entity.Player player = Bukkit.getPlayer(UUID.fromString(ctx.pathParam("uuid")));
        if (player != null) {
            player.updateInventory();
            Integer location = -1;
            for (org.bukkit.inventory.ItemStack itemStack : player.getInventory().getContents()) {
                location++;
                if (itemStack != null) {
                    ItemStack itemObj = new ItemStack();
                    // TODO: handle item namespaces other than minecraft: It's fine right now as there seem to be no forge + Paper servers
                    itemObj.setId("minecraft:" + itemStack.getType().toString().toLowerCase());
                    itemObj.setCount(Integer.valueOf(itemStack.getAmount()));
                    itemObj.setSlot(location);
                    inv.add(itemObj);
                }
            }
            ctx.json(inv);
        } else {
            try {
                String playerDatPath = Paths.get(new File("./").getAbsolutePath(), ctx.formParam("world"), "playerdata", ctx.formParam("uuid") + ".dat").toString();
                File playerfile = new File(playerDatPath);
                if(!playerfile.exists()){
                    throw new InternalServerErrorResponse(Constants.PLAYER_NOT_FOUND);
                }
                NBTFile playerFile = new NBTFile(playerfile);

                for (NBTListCompound item : playerFile.getCompoundList("Inventory")) {
                    ItemStack itemObj = new ItemStack();
                    itemObj.setId(item.getString("id"));
                    itemObj.setCount(item.getInteger("Count"));
                    itemObj.setSlot(item.getInteger("Slot"));
                    inv.add(itemObj);
                }

                ctx.json(inv);
            } catch (Exception e) {
                Bukkit.getLogger().warning(e.getMessage());
                throw new InternalServerErrorResponse(Constants.PLAYER_INV_PARSE_FAIL);
            }
        }

    }
}
