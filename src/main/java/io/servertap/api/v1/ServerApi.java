package io.servertap.api.v1;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.servertap.api.v1.models.Server;
import io.servertap.api.v1.models.ServerBan;
import io.servertap.api.v1.models.ServerHealth;
import io.servertap.api.v1.models.World;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ServerApi {

    private static final Logger log = Bukkit.getLogger();

    public static void ping(Context ctx) {
        ctx.json("pong");
    }

    public static void serverGet(Context ctx) {
        Server server = new Server();
        org.bukkit.Server bukkitServer = Bukkit.getServer();
        server.setName(bukkitServer.getName());
        server.setMotd(bukkitServer.getMotd());
        server.setVersion(bukkitServer.getVersion());
        server.setBukkitVersion(bukkitServer.getBukkitVersion());

        // Probably a better way to do this
        DecimalFormat df = new DecimalFormat("#.##");
        // Possibly add 5m and 15m in the future?
        if (bukkitServer.getTPS().length > 0) {
            server.setTps(df.format(bukkitServer.getTPS()[0]));
        } else {
            server.setTps("0.0");
        }


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

    public static void broadcastPost(Context ctx) {
        Bukkit.broadcastMessage(ctx.formParam("message"));
    }

    public static void worldsGet(Context ctx) {
        List<World> worlds = new ArrayList<>();
        Bukkit.getServer().getWorlds().forEach(world -> worlds.add(fromBukkitWorld(world)));

        ctx.json(worlds);
    }

    public static void worldGet(Context ctx) {
        org.bukkit.World bukkitWorld = Bukkit.getServer().getWorld(ctx.pathParam("world"));

        // 404 if no world found
        if (bukkitWorld == null) throw new NotFoundResponse();

        ctx.json(fromBukkitWorld(bukkitWorld));
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
