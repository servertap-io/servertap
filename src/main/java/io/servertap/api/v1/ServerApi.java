package io.servertap.api.v1;

import com.google.gson.Gson;
import io.servertap.api.v1.models.Server;
import io.servertap.api.v1.models.World;
import org.bukkit.Bukkit;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

        // TODO: Find a non-deprecated way to convert the difficulty
        world.setDifficulty(bukkitWorld.getDifficulty().getValue());

        world.setSeed(BigDecimal.valueOf(bukkitWorld.getSeed()));
        world.setStorm(bukkitWorld.hasStorm());
        world.setThundering(bukkitWorld.isThundering());

        return world;
    }

}
