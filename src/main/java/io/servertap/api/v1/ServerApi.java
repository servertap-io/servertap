package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.ServiceUnavailableResponse;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.utils.Lag;
import io.servertap.utils.ServerExecCommandSender;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

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
            methods = {HttpMethod.POST},
            summary = "Send broadcast visible to those currently online.",
            tags = {"Chat"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "message", type = "string")
                                    }
                            )
                    }
            ),
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
            methods = {HttpMethod.POST},
            summary = "Send a message to a specific player.",
            tags = {"Chat"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "message", type = "string"),
                                            @OpenApiContentProperty(name = "playerUuid", type = "string")
                                    }
                            )
                    }
            ),
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
            path = "/v1/scoreboard/{name}",
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
            methods = {HttpMethod.GET},
            summary = "Get the whitelist",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Whitelist.class))
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
            methods = {HttpMethod.POST},
            summary = "Update the whitelist",
            description = "Possible responses are: `success`, `failed`, `Error: duplicate entry`, and `No whitelist`.",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "uuid", type = "string"),
                                            @OpenApiContentProperty(name = "name", type = "string")
                                    }
                            )
                    }
            ),
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
            path = "/v1/server/whitelist",
            methods = {HttpMethod.DELETE},
            summary = "Delete specific player from the whitelist",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "uuid", type = "string"),
                                            @OpenApiContentProperty(name = "name", type = "string")
                                    }
                            )
                    }
            ),
            responses = {@OpenApiResponse(status = "200")}
    )
    public static void whitelistDelete(Context ctx) {
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
        Set<Whitelist> whitelist = getWhitelist();

        String finalUuid = uuid;
        whitelist.removeIf(entry -> entry.getUuid().toLowerCase().equals(finalUuid));

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
            methods = {HttpMethod.POST},
            summary = "Sets a specific player to Op",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "playerUuid", type = "string")
                                    }
                            )
                    }
            ),
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
            methods = {HttpMethod.DELETE},
            summary = "Removes Op from a specific player",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "playerUuid", type = "string")
                                    }
                            )
                    }
            ),
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
            methods = {HttpMethod.GET},
            summary = "Get all op players",
            tags = {"Player"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = @OpenApiContent(
                                    from = io.servertap.api.v1.models.OfflinePlayer.class
                            )
                    )
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
            methods = {HttpMethod.POST},
            summary = "Executes a command on the server from the console, returning it's output. Be aware that not all " +
                    "command executors will properly send their messages to the CommandSender, though, most do.",
            tags = {"Server"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "command", type = "string"),
                                            @OpenApiContentProperty(name = "time", type = "long")
                                    }
                            )
                    }
            ),
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
        if (StringUtils.isBlank(timeRaw)) {
            timeRaw = "0";
        }

        AtomicLong time = new AtomicLong(Long.parseLong(timeRaw));
        if (time.get() < 0) time.set(0);

        ctx.future(() -> runCommandAsync(command, time.get()).thenAccept(
                        output -> {
                            if ("application/json".equalsIgnoreCase(ctx.contentType())) {
                                ctx.json(output);
                            } else {
                                ctx.html(output);
                            }
                        }
                )
                .exceptionally(throwable -> {
                    throw new RuntimeException(throwable);
                }));
    }

    private static CompletableFuture<String> runCommandAsync(String command, long time) {
        return new ServerExecCommandSender().executeCommand(command, time, TimeUnit.MILLISECONDS);
    }

}
