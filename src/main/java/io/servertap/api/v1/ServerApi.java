package io.servertap.api.v1;

import com.google.gson.Gson;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.ServiceUnavailableResponse;
import io.javalin.plugin.openapi.annotations.*;
import io.servertap.Constants;
import io.servertap.Lag;
import io.servertap.ServerExecCommandSender;
import io.servertap.api.v1.models.*;
import io.servertap.mojang.api.MojangApiService;
import io.servertap.mojang.api.models.NameChange;
import io.servertap.utils.EconomyWrapper;
import io.servertap.utils.GsonSingleton;
import org.apache.commons.lang.StringUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class ServerApi {

    private static final Logger log = Bukkit.getLogger();

    @OpenApi(
            path = "/v1/ping",
            summary = "pong!",
            operationId = "ping",
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
            operationId = "getServerInfo",
            summary = "Get information about the server",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Server.class, type = "application/json"))
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
        server.setMaxPlayers(bukkitServer.getMaxPlayers());
        server.setOnlinePlayers(bukkitServer.getOnlinePlayers().size());

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
            path = "/v1/chat/broadcast",
            method = HttpMethod.POST,
            operationId = "broadcastMessage",
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
            operationId = "tellPlayer",
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
            operationId = "getScoreBoard",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Scoreboard.class, type = "application/json"))
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
            path = "/v1/scoreboard/{name}",
            summary = "Get information about a specific objective",
            operationId = "getScoreboardByName",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            pathParams = {
                    @OpenApiParam(name = "name", description = "The name of the objective to get")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Objective.class, type = "application/json"))
            }
    )
    public static void objectiveGet(Context ctx) {
        String objectiveName = ctx.pathParam("name");
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
            operationId = "getWhitelist",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Whitelist.class, isArray = true, type = "application/json"))
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
            operationId = "addToWhitelist",
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
        final String json = GsonSingleton.getInstance().toJson(whitelist);
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
            path = "/v1/server/ops",
            method = HttpMethod.POST,
            summary = "Sets a specific player to Op",
            operationId = "opPlayer",
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
            operationId = "deopPlayer",
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
            operationId = "getOps",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(from = io.servertap.api.v1.models.OfflinePlayer.class, isArray = true, type = "application/json"))
            }
    )
    public static void getOps(Context ctx) {
        Set<org.bukkit.OfflinePlayer> players = Bukkit.getOperators();
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

            if (EconomyWrapper.getInstance().getEconomy() != null) {
                p.setBalance(EconomyWrapper.getInstance().getEconomy().getBalance(player));
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
            operationId = "executeCommand",
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

        ctx.future(CompletableFuture.supplyAsync(() -> {
            CompletableFuture<String> future = new ServerExecCommandSender().executeCommand(command, time.get(), TimeUnit.MILLISECONDS);
            try {
                String output = future.get();
                Gson g = GsonSingleton.getInstance();

                return "application/json".equalsIgnoreCase(ctx.contentType()) ? g.toJson(output) : output;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }));
    }

}
