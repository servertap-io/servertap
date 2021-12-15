package io.servertap.api.v1;

import com.google.gson.Gson;
import io.javalin.http.*;
import io.javalin.plugin.json.JavalinJson;
import io.javalin.plugin.openapi.annotations.*;
import io.servertap.Constants;
import io.servertap.Lag;
import io.servertap.PluginEntrypoint;
import io.servertap.ServerExecCommandSender;
import io.servertap.api.v1.models.*;
import io.servertap.mojang.api.MojangApiService;
import io.servertap.mojang.api.models.NameChange;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerApi {

    private static final Logger log = Bukkit.getLogger();

    @OpenApi(
            path = "/v1/ping",
            summary = "pong!",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json"))
            }
    )
    public static void ping(Context ctx) {
        ctx.json("pong");
    }

    @OpenApi(
            path = "/v1/server",
            summary = "Get information about the server",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Server.class))
            }
    )
    public static void serverGet(Context ctx) {
        Server server = new Server();
        org.bukkit.Server bukkitServer = Bukkit.getServer();
        server.setName(bukkitServer.getName());
        server.setMotd(bukkitServer.getMotd());
        server.setVersion(bukkitServer.getVersion());
        server.setBukkitVersion(bukkitServer.getBukkitVersion());
        server.setWhitelistedPlayers(getWhitelist());

        // Possibly add 5m and 15m in the future?
        server.setTps(Lag.getTPSString());

        // Get the list of IP bans
        Set<ServerBan> bannedIps = new HashSet<>();
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
        Set<ServerBan> bannedPlayers = new HashSet<>();
        bukkitServer.getBanList(BanList.Type.NAME).getBanEntries().forEach(banEntry -> {
            ServerBan ban = new ServerBan();

            ban.setSource(banEntry.getSource());
            ban.setExpiration(banEntry.getExpiration());
            ban.setReason(ban.getReason());
            ban.setTarget(banEntry.getTarget());

            bannedPlayers.add(ban);
        });
        server.setBannedPlayers(bannedPlayers);

        ServerHealth health = new ServerHealth();

        // Logical CPU count
        int cpus = Runtime.getRuntime().availableProcessors();
        health.setCpus(cpus);

        // Uptime
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000L;
        health.setUptime(uptime);

        // Memory stats from the runtime
        long memMax = Runtime.getRuntime().maxMemory();
        long memTotal = Runtime.getRuntime().totalMemory();
        long memFree = Runtime.getRuntime().freeMemory();
        health.setMaxMemory(memMax);
        health.setTotalMemory(memTotal);
        health.setFreeMemory(memFree);

        server.setHealth(health);

        ctx.json(server);
    }

    @OpenApi(
            path = "/v1/worlds/save",
            summary = "Triggers a world save of all worlds",
            method = HttpMethod.POST,
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200")
            }
    )
    public static void saveAllWorlds(Context ctx) {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        Plugin pluginInstance = bukkitServer.getPluginManager().getPlugin("ServerTap");

        if (pluginInstance != null) {
            // Run the saves on the main thread, can't use sync methods from here otherwise
            bukkitServer.getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> {
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    try {
                        world.save();
                    } catch (Exception e) {
                        // Just warn about the issue
                        log.warning(String.format("Couldn't save World %s %s", world.getName(), e.getMessage()));
                    }
                }
            });
        }

        ctx.json("success");
    }

    @OpenApi(
            path = "/v1/worlds/:uuid/save",
            summary = "Triggers a world save",
            method = HttpMethod.POST,
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the World to save")
            },
            responses = {
                    @OpenApiResponse(status = "200")
            }
    )
    public static void saveWorld(Context ctx) {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        UUID worldUUID = ValidationUtils.safeUUID(ctx.pathParam("uuid"));
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.World world = bukkitServer.getWorld(worldUUID);

        if (world != null) {
            Plugin pluginInstance = bukkitServer.getPluginManager().getPlugin("ServerTap");

            if (pluginInstance != null) {
                // Run the saves on the main thread, can't use sync methods from here otherwise
                bukkitServer.getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> {

                    try {
                        world.save();
                    } catch (Exception e) {
                        // Just warn about the issue
                        log.warning(String.format("Couldn't save World %s %s", world.getName(), e.getMessage()));
                    }
                });
            }
        }

        ctx.json("success");
    }

    private static void addFolderToZip(File folder, ZipOutputStream zip, String baseName, String rootName) throws IOException {
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                if(rootName == null) {
                    addFolderToZip(file, zip, baseName, folder.getName());
                } else {
                    addFolderToZip(file, zip, baseName, rootName);
                }
            } else {
                String name = file.getAbsolutePath().substring(baseName.length())
                        // Trim first slash (absolute path)
                        .replaceFirst("^/", "");
//              Join path and folder name
                if(rootName == null) {
                    name = folder.getName() + "/" + name;
                } else {
                    name = rootName + "/" + name;
                }
                ZipEntry zipEntry = new ZipEntry(name);
                zip.putNextEntry(zipEntry);
                IOUtils.copy(new FileInputStream(file), zip);
                zip.closeEntry();
            }
        }
    }

    @OpenApi(
            path = "/v1/worlds/:uuid/download",
            summary = "Downloads a Zip compressed archive of the world's folder",
            method = HttpMethod.GET,
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The UUID of the World to download")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/zip")),
            }
    )
    public static void downloadWorld(Context ctx) throws IOException {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        UUID worldUUID = ValidationUtils.safeUUID(ctx.pathParam("uuid"));
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.World world = bukkitServer.getWorld(worldUUID);

        if (world != null) {
            File folder = world.getWorldFolder();

            // Set headers for proper zip download
            ctx.header("Content-Disposition", "attachment; filename=\"" + folder.getName() + ".zip\"");
            ctx.header("Content-Type", "application/zip");


            ZipOutputStream zip = new ZipOutputStream(ctx.res.getOutputStream());
            addFolderToZip(folder, zip, folder.getAbsolutePath(), null);
            zip.close();
        }
    }

    @OpenApi(
            path = "/v1/worlds/download",
            summary = "Downloads a Zip compressed archive of all the worlds' folders",
            method = HttpMethod.GET,
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/zip")),
            }
    )
    public static void downloadWorlds(Context ctx) throws IOException {
        org.bukkit.Server bukkitServer = Bukkit.getServer();

        Plugin pluginInstance = bukkitServer.getPluginManager().getPlugin("ServerTap");

        ctx.header("Content-Disposition", "attachment; filename=\"worlds.zip\"");
        ctx.header("Content-Type", "application/zip");

        ZipOutputStream zip = new ZipOutputStream(ctx.res.getOutputStream());

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            try {

                File folder = world.getWorldFolder();

                addFolderToZip(folder, zip, folder.getAbsolutePath(), null);

            } catch (Exception e) {
                // Just warn about the issue
                log.warning(String.format("Couldn't save World %s %s", world.getName(), e.getMessage()));
                throw new InternalServerErrorResponse("Couldn't download world " + world.getName() + ": " + e.getMessage());
            }
        }
        try {
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OpenApi(
            path = "/v1/chat/broadcast",
            method = HttpMethod.POST,
            summary = "Send broadcast visible to those currently online.",
            tags = {"Chat"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            formParams = {
                    @OpenApiFormParam(name = "message")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json"))
            }
    )
    public static void broadcastPost(Context ctx) {
        if (ctx.formParam("message").isEmpty()) {
            throw new BadRequestResponse(Constants.CHAT_MISSING_MESSAGE);
        }
        Bukkit.broadcastMessage(ctx.formParam("message"));
        ctx.json("success");
    }

    @OpenApi(
            path = "/v1/chat/tell",
            method = HttpMethod.POST,
            summary = "Send a message to a specific player.",
            tags = {"Chat"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            formParams = {
                    @OpenApiFormParam(name = "message", type = String.class),
                    @OpenApiFormParam(name = "playerUuid", type = String.class)
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json"))
            }
    )
    public static void tellPost(Context ctx) {
        if (ctx.formParam("message").isEmpty()) {
            throw new BadRequestResponse(Constants.CHAT_MISSING_MESSAGE);
        }
        if (ctx.formParam("playerUuid").isEmpty()) {
            throw new BadRequestResponse(Constants.PLAYER_UUID_MISSING);
        }

        UUID playerUUID = ValidationUtils.safeUUID(ctx.formParam("playerUuid"));
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.entity.Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);
        }
        player.sendMessage(ctx.formParam("message"));

        ctx.json("success");
    }

    @OpenApi(
            path = "/v1/scoreboard",
            summary = "Get information about the scoreboard objectives",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Scoreboard.class))
            }
    )

    public static void scoreboardGet(Context ctx) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard gameScoreboard = manager.getMainScoreboard();
        Scoreboard scoreboardModel = new Scoreboard();
        Set<String> objectives = new HashSet<>();

        Set<String> entries = new HashSet<>(gameScoreboard.getEntries());

        gameScoreboard.getObjectives().forEach(objective -> objectives.add(objective.getName()));

        scoreboardModel.setEntries(entries);
        scoreboardModel.setObjectives(objectives);

        ctx.json(scoreboardModel);
    }

    @OpenApi(
            path = "/v1/scoreboard/:name",
            summary = "Get information about a specific objective",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "name", description = "The name of the objective to get")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Objective.class))
            }
    )
    public static void objectiveGet(Context ctx) {
        String objectiveName = ctx.pathParam(":name");
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard gameScoreboard = manager.getMainScoreboard();
        org.bukkit.scoreboard.Objective objective = gameScoreboard.getObjective(objectiveName);

        if (objective == null) {
            throw new NotFoundResponse();
        }

        ctx.json(fromBukkitObjective(objective));
    }

    private static Objective fromBukkitObjective(org.bukkit.scoreboard.Objective objective) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        org.bukkit.scoreboard.Scoreboard gameScoreboard = manager.getMainScoreboard();

        Objective o = new Objective();
        o.setCriterion(objective.getCriteria());
        o.setDisplayName(objective.getDisplayName());
        o.setName(objective.getName());

        o.setDisplaySlot("");
        if (objective.getDisplaySlot() != null) {
            o.setDisplaySlot(objective.getDisplaySlot().toString().toLowerCase());
        }

        Set<Score> scores = new HashSet<>();
        gameScoreboard.getEntries().forEach(entry -> {
            org.bukkit.scoreboard.Score score = objective.getScore(entry);

            if (score.isScoreSet()) {
                Score s = new Score();
                s.setEntry(entry);
                s.setValue(score.getScore());

                scores.add(s);
            }
        });
        o.setScores(scores);

        return o;
    }

    @OpenApi(
            path = "/v1/worlds",
            summary = "Get information about all worlds",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = World.class, isArray = true))
            }
    )
    public static void worldsGet(Context ctx) {
        List<World> worlds = new ArrayList<>();
        Bukkit.getServer().getWorlds().forEach(world -> worlds.add(fromBukkitWorld(world)));

        ctx.json(worlds);
    }

    @OpenApi(
            path = "/v1/worlds/:uuid",
            summary = "Get information about a specific world",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "uuid", description = "The uuid of the world")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = World.class))
            }
    )
    public static void worldGet(Context ctx) {

        UUID worldUUID = ValidationUtils.safeUUID(ctx.pathParam("uuid"));
        if (worldUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.World bukkitWorld = Bukkit.getServer().getWorld(worldUUID);

        // 404 if no world found
        if (bukkitWorld == null) throw new NotFoundResponse();

        ctx.json(fromBukkitWorld(bukkitWorld));
    }

    private static World fromBukkitWorld(org.bukkit.World bukkitWorld) {
        World world = new World();

        world.setName(bukkitWorld.getName());
        world.setUuid(bukkitWorld.getUID().toString());

        // TODO: The Enum for Environment makes this annoying to get
        switch (bukkitWorld.getEnvironment()) {
            case NORMAL:
                world.setEnvironment(0);
                break;
            case NETHER:
                world.setEnvironment(-1);
                break;
            case THE_END:
                world.setEnvironment(1);
                break;
            default:
                world.setEnvironment(0);
                break;
        }

        world.setTime(BigDecimal.valueOf(bukkitWorld.getTime()));
        world.setAllowAnimals(bukkitWorld.getAllowAnimals());
        world.setAllowMonsters(bukkitWorld.getAllowMonsters());
        world.setGenerateStructures(bukkitWorld.canGenerateStructures());

        int value = 0;
        switch (bukkitWorld.getDifficulty()) {
            case PEACEFUL:
                value = 0;
                break;
            case EASY:
                value = 1;
                break;
            case NORMAL:
                value = 3;
                break;
            case HARD:
                value = 2;
                break;
        }
        world.setDifficulty(value);

        world.setSeed(BigDecimal.valueOf(bukkitWorld.getSeed()));
        world.setStorm(bukkitWorld.hasStorm());
        world.setThundering(bukkitWorld.isThundering());

        return world;
    }

    private static Set<Whitelist> getWhitelist() {
        Set<Whitelist> whitelist = new HashSet<Whitelist>();
        Bukkit.getServer().getWhitelistedPlayers().forEach((OfflinePlayer player) -> {
            whitelist.add(new Whitelist().offlinePlayer(player));
        });
        return whitelist;
    }

    @OpenApi(
            path = "/v1/server/whitelist",
            method = HttpMethod.GET,
            summary = "Get the whitelist",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Whitelist.class, isArray = true))
            }
    )
    public static void whitelistGet(Context ctx) {
        if (!Bukkit.getServer().hasWhitelist()) {
            // TODO: Handle Errors better
            ctx.json("error: The server has whitelist disabled");
            return;
        }
        ctx.json(getWhitelist());
    }

    @OpenApi(
            path = "/v1/server/whitelist",
            method = HttpMethod.POST,
            summary = "Update the whitelist",
            description = "Possible responses are: `success`, `failed`, `Error: duplicate entry`, and `No whitelist`.",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            formParams = {
                    @OpenApiFormParam(name = "uuid", type = String.class),
                    @OpenApiFormParam(name = "name", type = String.class)
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json"))
            }
    )
    public static void whitelistPost(Context ctx) {
        final org.bukkit.Server bukkitServer = Bukkit.getServer();
        if (!bukkitServer.hasWhitelist()) {
            ctx.json("No whitelist");
            return;
        }

        String uuid = ctx.formParam("uuid");
        String name = ctx.formParam("name");

        if (uuid == null && name == null) {
            throw new BadRequestResponse(Constants.WHITELIST_MISSING_PARAMS);
        }

        //Check Mojang API for missing param
        if (uuid == null) {
            try {
                uuid = MojangApiService.getUuid(name);
            } catch (IllegalArgumentException ignored) {
                throw new NotFoundResponse(Constants.WHITELIST_NAME_NOT_FOUND);
            } catch (IOException ignored) {
                throw new ServiceUnavailableResponse(Constants.WHITELIST_MOJANG_API_FAIL);
            }
        } else if (name == null) {
            try {
                List<NameChange> nameHistory = MojangApiService.getNameHistory(uuid);
                name = nameHistory.get(nameHistory.size() - 1).getName();
            } catch (IllegalArgumentException ignored) {
                throw new NotFoundResponse(Constants.WHITELIST_UUID_NOT_FOUND);
            } catch (IOException ignored) {
                throw new ServiceUnavailableResponse(Constants.WHITELIST_MOJANG_API_FAIL);
            }
        }

        //Whitelist file doesn't accept UUIDs without dashes
        uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

        final File directory = new File("./");

        final Whitelist newEntry = new Whitelist().uuid(uuid).name(name);
        Set<Whitelist> whitelist = getWhitelist();
        for (Whitelist player : whitelist) {
            if (player.equals(newEntry)) {
                ctx.json("Error: duplicate entry");
                return;
            }
        }
        whitelist.add(newEntry);
        final String json = new Gson().toJson(whitelist);
        try {
            final String path = Paths.get(directory.getAbsolutePath(), "whitelist.json").toString();
            final File myObj = new File(path);
            final FileWriter whitelistFile = new FileWriter(myObj);
            whitelistFile.write(json);
            whitelistFile.close();
            bukkitServer.reloadWhitelist();
            ctx.json("success");
        } catch (final IOException e) {
            log.warning("An error occurred updating whitelist.");
            e.printStackTrace();
            ctx.json("failed");
        }
    }

    @OpenApi(
            path = "/v1/plugins",
            method = HttpMethod.GET,
            summary = "Get a list of installed plugins",
            description = "Responds with an array of objects containing keys name and enabled.",
            tags = {"Plugins"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json"))
            }
    )
    public static void listPlugins(Context ctx) {
        ArrayList<io.servertap.api.v1.models.Plugin> pluginList = new ArrayList<>();
        for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

            io.servertap.api.v1.models.Plugin pl = new io.servertap.api.v1.models.Plugin();
            pl.setName(plugin.getName());
            pl.setEnabled(plugin.isEnabled());
            pl.setVersion(plugin.getDescription().getVersion());
            pl.setAuthors(plugin.getDescription().getAuthors());
            pl.setDescription(plugin.getDescription().getDescription());

            pluginList.add(pl);
        }

        ctx.json(pluginList);
    }

    @OpenApi(
            path = "/v1/server/ops",
            method = HttpMethod.POST,
            summary = "Sets a specific player to Op",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            formParams = {
                    @OpenApiFormParam(name = "playerUuid"),
            },
            responses = {
                    @OpenApiResponse(status = "200")
            })
    public static void opPlayer(Context ctx) {
        if (ctx.formParam("playerUuid").isEmpty()) {
            throw new BadRequestResponse(Constants.PLAYER_MISSING_PARAMS);
        }

        UUID playerUUID = ValidationUtils.safeUUID(ctx.formParam("playerUuid"));
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        if (player == null) {
            throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);
        }
        player.setOp(true);
        ctx.json("success");
    }

    @OpenApi(
            path = "/v1/server/ops",
            method = HttpMethod.DELETE,
            summary = "Removes Op from a specific player",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            formParams = {
                    @OpenApiFormParam(name = "playerUuid")
            },
            responses = {@OpenApiResponse(status = "200")}
    )
    public static void deopPlayer(Context ctx) {
        if (ctx.formParam("playerUuid").isEmpty()) {
            throw new BadRequestResponse(Constants.PLAYER_MISSING_PARAMS);
        }

        UUID playerUUID = ValidationUtils.safeUUID(ctx.formParam("playerUuid"));
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }

        org.bukkit.OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        if (player == null) {
            throw new NotFoundResponse(Constants.PLAYER_NOT_FOUND);
        }
        player.setOp(false);
        ctx.json("success");
    }

    @OpenApi(
            path = "/v1/server/ops",
            method = HttpMethod.GET,
            summary = "Get all op players",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = io.servertap.api.v1.models.OfflinePlayer.class, isArray = true))
            }
    )
    public static void getOps(Context ctx) {
        org.bukkit.OfflinePlayer players[] = Bukkit.getOfflinePlayers();
        ArrayList<io.servertap.api.v1.models.OfflinePlayer> opedPlayers = new ArrayList<io.servertap.api.v1.models.OfflinePlayer>();
        for (org.bukkit.OfflinePlayer player : players) {
            if (!player.isOp()) {
                continue;
            }
            io.servertap.api.v1.models.OfflinePlayer p = new io.servertap.api.v1.models.OfflinePlayer();
            p.setDisplayName(player.getName());
            p.setUuid(player.getUniqueId().toString());
            p.setWhitelisted(player.isWhitelisted());
            p.setBanned(player.isBanned());
            p.setOp(player.isOp());

            if (PluginEntrypoint.getEconomy() != null) {
                p.setBalance(PluginEntrypoint.getEconomy().getBalance(player));
            }

            opedPlayers.add(p);

        }
        ctx.json(opedPlayers);

    }

    @OpenApi(
            path = "/v1/server/exec",
            method = HttpMethod.POST,
            summary = "Executes a command on the server from the console, returning it's output. Be aware that not all " +
                    "command executors will properly send their messages to the CommandSender, though, most do.",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            formParams = {
                    @OpenApiFormParam(name = "command", required = true),
                    @OpenApiFormParam(name = "time", type = Long.class)
            },
            responses = {
                    @OpenApiResponse(
                            status = "200"
                    )
            }
    )
    public static void postCommand(Context ctx) {
        String command = ctx.formParam("command");
        if (StringUtils.isBlank(command)) {
            throw new BadRequestResponse(Constants.COMMAND_PAYLOAD_MISSING);
        }

        String timeRaw = ctx.formParam("time");
        AtomicLong time = new AtomicLong(timeRaw != null ? Long.parseLong(timeRaw) : 0);
        if (time.get() < 0) time.set(0);

        ctx.result(CompletableFuture.supplyAsync(() -> {
            CompletableFuture<String> future = new ServerExecCommandSender().executeCommand(command, time.get(), TimeUnit.MILLISECONDS);
            try {
                String output = future.get();
                return "application/json".equalsIgnoreCase(ctx.contentType()) ? JavalinJson.toJson(output) : output;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }));
    }

}
