package io.servertap;

import com.google.gson.Gson;
import io.servertap.api.v1.PlayerApi;
import io.servertap.api.v1.ServerApi;
import io.servertap.gen.models.Player;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static spark.Spark.*;

public class PluginEntrypoint extends JavaPlugin {

    private final String CTYPE = "application/json";
    private final Logger log = getLogger();

    @Override
    public void onEnable() {
        Gson gson = new Gson();

        // Standard request logger
        before("/*", (req, res) -> log.info("Request to " + req.pathInfo()));

        //Routes for v1 of the API
        path(Constants.API_VERSION, () -> {
            // Pings
            get("/ping", CTYPE, ServerApi::ping, gson::toJson);
            post("/ping", CTYPE, ServerApi::ping, gson::toJson);

            // Server routes
            get("/server", CTYPE, ServerApi::base, gson::toJson);
            get("/worlds", CTYPE, ServerApi::worlds, gson::toJson);
            get("/worlds/:world", CTYPE, ServerApi::world, gson::toJson);

            // Communication
            post("/broadcast", CTYPE, ServerApi::broadcast, gson::toJson);

            // Player routes
            get("/players", CTYPE, PlayerApi::players, gson::toJson);
        });

        // Default fallthrough. Just give them a 404.
        get("/*", (req, res) -> halt(404, "Nothing here"));
    }

}
