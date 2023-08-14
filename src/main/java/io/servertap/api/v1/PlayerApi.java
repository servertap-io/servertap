package io.servertap.api.v1;

import de.tr7zw.nbtapi.NBTFile;
import io.javalin.http.*;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.Player;
import io.servertap.utils.pluginwrappers.EconomyWrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerApi {
    private final EconomyWrapper economy;
    private final Logger log;

    public PlayerApi(Logger log, EconomyWrapper economy) {
        this.economy = economy;
        this.log = log;
    }

    @OpenApi(
            path = "/v1/players",
            summary = "Gets all currently online players",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Player.class))
            }
    )
    public void playersGet(Context ctx) {
        ctx.json(getOninePlayers());
    }

    public ArrayList<Player> getOninePlayers() {
        ArrayList<Player> players = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> players.add(Player.fromBukkitPlayer(player, economy)));
        return players;
    }

    @OpenApi(
            path = "/v1/players/{uuid}",
            methods = {HttpMethod.GET},
            summary = "Gets a specific online player by their UUID",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "UUID of the player")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Player.class))
            }
    )
    public void playerGet(Context ctx) {
        String uuid = ctx.pathParam("uuid");

        if (uuid.isEmpty()) throw new BadRequestResponse(Constants.PLAYER_UUID_MISSING);

        UUID playerUUID = ValidationUtils.safeUUID(uuid);
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.entity.Player player = Bukkit.getPlayer(playerUUID);

        if (player == null) throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);

        ctx.json(Player.fromBukkitPlayer(player, economy));
    }

    @OpenApi(
            path = "/v1/players/all",
            summary = "Gets all players that have ever joined the server ",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = io.servertap.api.v1.models.OfflinePlayer.class))
            }
    )
    public void offlinePlayersGet(Context ctx) {
        ctx.json(getAllPlayers());
    }

    public ArrayList<io.servertap.api.v1.models.OfflinePlayer> getAllPlayers() {
        ArrayList<io.servertap.api.v1.models.OfflinePlayer> players = new ArrayList<>();
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for (OfflinePlayer offlinePlayer : offlinePlayers)
            players.add(io.servertap.api.v1.models.OfflinePlayer.getFromBukkitOfflinePlayer(offlinePlayer, economy));
        return players;
    }

    @OpenApi(
            path = "/v1/players/{playerUuid}/{worldUuid}/inventory",
            methods = {HttpMethod.GET},
            summary = "Gets a specific online player's Inventory in the specified world",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "playerUuid", description = "UUID of the player"),
                    @OpenApiParam(name = "worldUuid", description = "UUID of the world")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = io.servertap.api.v1.models.ItemStack.class))
            }
    )
    public void getPlayerInv(Context ctx) {
        String playerUUIDStr = ctx.pathParam("playerUuid");
        String worldUUIDStr = ctx.pathParam("worldUuid");
        if (playerUUIDStr.isEmpty() || worldUUIDStr.isEmpty()) {
            throw new BadRequestResponse(Constants.PLAYER_MISSING_PARAMS);
        }

        UUID playerUUID = ValidationUtils.safeUUID(playerUUIDStr);
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }
        UUID worldUUID = ValidationUtils.safeUUID(worldUUIDStr);
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        ctx.json(getPlayerInv(playerUUID, worldUUID));
    }

    public ArrayList<ItemStack> getPlayerInv(UUID playerUUID, UUID worldUUID) {
        ArrayList<ItemStack> inv = new ArrayList<>();
        org.bukkit.entity.Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            Integer location = -1;
            for (org.bukkit.inventory.ItemStack itemStack : player.getInventory().getContents()) {
                location++;
                if (itemStack != null) {
                    ItemStack itemObj = ItemStack.fromBukkitItemStack(itemStack);
                    itemObj.setSlot(location);
                    inv.add(itemObj);
                }
            }
            return inv;
        }

        try {
            World bukWorld = Bukkit.getWorld(worldUUID);

            if (bukWorld == null) throw new NotFoundResponse(Constants.WORLD_NOT_FOUND);

            String dataPath = String.format(
                    "%s/%s/playerdata/%s.dat",
                    new File("./").getAbsolutePath(),
                    bukWorld.getName(),
                    playerUUID
            );

            File playerfile = new File(Paths.get(dataPath).toString());
            if (!playerfile.exists()) throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);
            NBTFile playerFile = new NBTFile(playerfile);

            playerFile.getCompoundList("Inventory").forEach(item -> {
                ItemStack itemObj = new ItemStack();
                itemObj.setId(item.getString("id"));
                itemObj.setCount(item.getInteger("Count"));
                itemObj.setSlot(item.getInteger("Slot"));
                inv.add(itemObj);
            });

            return inv;

        } catch (Exception e) {
            log.warning(e.getMessage());
            throw new InternalServerErrorResponse(Constants.PLAYER_INV_PARSE_FAIL);
        }
    }
}
