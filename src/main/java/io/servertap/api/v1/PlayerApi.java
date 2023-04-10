package io.servertap.api.v1;

import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTListCompound;
import io.javalin.http.*;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.Player;
import io.servertap.utils.EconomyWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerApi {

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
    public static void playersGet(Context ctx) {
        ArrayList<Player> players = new ArrayList<>();

        Bukkit.getOnlinePlayers().forEach((player -> {
            players.add(getPlayer(player));
        }));

        ctx.json(players);
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
    public static void playerGet(Context ctx) {
        if (ctx.pathParam("uuid").isEmpty()) {
            throw new BadRequestResponse(Constants.PLAYER_UUID_MISSING);
        }

        UUID playerUUID = ValidationUtils.safeUUID(ctx.pathParam("uuid"));
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.entity.Player player = Bukkit.getPlayer(playerUUID);

        if (player == null) {
            throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);
        }

        ctx.json(getPlayer(player));
    }

    /**
     * Internal method to convert a Bukkit player into a ServerTap player
     *
     * @param player The Bukkit player
     * @return The ServerTap player
     */
    private static Player getPlayer(org.bukkit.entity.Player player) {
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

        if (EconomyWrapper.getInstance().getEconomy() != null) {
            p.setBalance(EconomyWrapper.getInstance().getEconomy().getBalance(player));
        }

        p.setHunger(player.getFoodLevel());
        p.setHealth(player.getHealth());
        p.setSaturation(player.getSaturation());

        p.setDimension(player.getWorld().getEnvironment().toString());

        Location playerLocation = player.getLocation();
        Double[] convertedLocation = new Double[3];
        convertedLocation[0] = playerLocation.getX();
        convertedLocation[1] = playerLocation.getY();
        convertedLocation[2] = playerLocation.getZ();

        p.setLocation(convertedLocation);

        p.setGamemode(player.getGameMode().toString());

        p.setLastPlayed(player.getLastPlayed());

        return p;
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

            if (EconomyWrapper.getInstance().getEconomy() != null) {
                p.setBalance(EconomyWrapper.getInstance().getEconomy().getBalance(offlinePlayers[i]));
            }

            p.setLastPlayed(player.getLastPlayed());

            players.add(p);
        }

        ctx.json(players);
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
    public static void getPlayerInv(Context ctx) {
        if (ctx.pathParam("playerUuid").isEmpty() || ctx.pathParam("worldUuid").isEmpty()) {
            throw new BadRequestResponse(Constants.PLAYER_MISSING_PARAMS);
        }

        UUID playerUUID = ValidationUtils.safeUUID(ctx.pathParam("playerUuid"));
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }
        UUID worldUUID = ValidationUtils.safeUUID(ctx.pathParam("worldUuid"));
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        ArrayList<ItemStack> inv = new ArrayList<ItemStack>();
        org.bukkit.entity.Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            player.updateInventory();
            Integer location = -1;
            for (org.bukkit.inventory.ItemStack itemStack : player.getInventory().getContents()) {
                location++;
                if (itemStack != null) {
                    ItemStack itemObj = new ItemStack();
                    // TODO: handle item namespaces other than minecraft: It's fine right now as there seem to be no forge + Paper servers
                    itemObj.setId("minecraft:" + itemStack.getType().toString().toLowerCase());
                    itemObj.setCount(itemStack.getAmount());
                    itemObj.setSlot(location);
                    inv.add(itemObj);
                }
            }
            ctx.json(inv);
        } else {
            try {
                World bukWorld = Bukkit.getWorld(worldUUID);

                if (bukWorld == null) {
                    throw new NotFoundResponse(Constants.WORLD_NOT_FOUND);
                }

                String dataPath = String.format(
                        "%s/%s/playerdata/%s.dat",
                        new File("./").getAbsolutePath(),
                        bukWorld.getName(),
                        ctx.pathParam("playerUuid")
                );
                File playerfile = new File(Paths.get(dataPath).toString());
                if (!playerfile.exists()) {
                    throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);
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

            } catch (HttpResponseException e) {
                // Pass any javalin exceptions up the chain
                throw e;
            } catch (Exception e) {
                Bukkit.getLogger().warning(e.getMessage());
                throw new InternalServerErrorResponse(Constants.PLAYER_INV_PARSE_FAIL);
            }
        }

    }
}
