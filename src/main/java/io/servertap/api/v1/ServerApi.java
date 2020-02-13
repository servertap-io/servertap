package io.servertap.api.v1;

import com.google.gson.Gson;
import io.servertap.api.v1.models.Server;
import io.servertap.api.v1.models.ServerBan;
import io.servertap.api.v1.models.ServerHealth;
import io.servertap.api.v1.models.World;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static spark.Spark.halt;

public class ServerApi {

    private final Gson gson = new Gson();
    private static final Logger log = Bukkit.getLogger();

    public static Object ping(Request req, Response res) {
        res.type("application/json");
        return "pong";
    }

    public static Object base(Request request, Response response) {
        response.type("application/json");

        Server server = new Server();
        org.bukkit.Server bukkitServer = Bukkit.getServer();
        server.setName(bukkitServer.getName());
        server.setMotd(bukkitServer.getMotd());
        server.setVersion(bukkitServer.getVersion());
        server.setBukkitVersion(bukkitServer.getBukkitVersion());

        // Probably a better way to do this
        DecimalFormat df = new DecimalFormat("#.##");
        // Possibly add 5m and 15m in the future?
        server.setTps(df.format(String.valueOf(bukkitServer.getTPS()[0])));

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
        return server;
    }

    public static Object broadcast(Request request, Response response) {
        response.type("application/json");

        Bukkit.broadcastMessage(request.queryParams("message"));
        return null;
    }

    public static Object worlds(Request request, Response response) {
        response.type("application/json");

        List<World> worlds = new ArrayList<>();
        Bukkit.getServer().getWorlds().forEach(world -> worlds.add(fromBukkitWorld(world)));

        return worlds;
    }

    public static Object world(Request request, Response response) {
        response.type("application/json");

        org.bukkit.World bukkitWorld = Bukkit.getServer().getWorld(request.params(":world"));

        // 404 if no world found
        if (bukkitWorld == null) halt(404, "World not found");

        return fromBukkitWorld(bukkitWorld);
    }

    private static World fromBukkitWorld(org.bukkit.World bukkitWorld) {
        World world = new World();

        world.setName(bukkitWorld.getName());

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
}
