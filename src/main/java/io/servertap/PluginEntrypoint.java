package io.servertap;

import io.javalin.Javalin;
import io.javalin.core.security.Role;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import io.servertap.api.v1.PlayerApi;
import io.servertap.api.v1.ServerApi;
import io.servertap.api.v1.auth.ServerTapJWTProvider;
import javalinjwt.JWTAccessManager;
import javalinjwt.JavalinJWT;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static io.javalin.apibuilder.ApiBuilder.*;

public class PluginEntrypoint extends JavaPlugin {

    private final Logger log = getLogger();

    enum Roles implements Role {
        ANYONE,
        USER,
        ADMIN
    }

    Map<String, Role> rolesMapping = new HashMap<String, Role>() {{
        put("user", Roles.USER);
        put("admin", Roles.ADMIN);
    }};

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        String jwtSecret = this.getConfig().getString("jwtSecret");
        ServerTapJWTProvider provider = new ServerTapJWTProvider(jwtSecret);

        // Get the current class loader.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Temporarily set this thread's class loader to the plugin's class loader.
        // Replace JavalinTestPlugin.class with your own plugin's class.
        Thread.currentThread().setContextClassLoader(PluginEntrypoint.class.getClassLoader());

        JWTAccessManager accessManager = new JWTAccessManager("level", rolesMapping, Roles.ANYONE);

        // Instantiate the web server (which will now load using the plugin's class loader).
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.accessManager(accessManager);
        }).start(4567);

        // Log every request
        app.before(ctx -> log.info(ctx.req.getPathInfo()));

        // Enforce auth for every route
        Handler decodeHandler = JavalinJWT.createHeaderDecodeHandler(provider.getProvider());
        app.before(decodeHandler);

        app.routes(() -> {
            //Routes for v1 of the API
            path(Constants.API_V1, () -> {
                // Pings
                get("ping", ServerApi::ping);
                post("ping", ServerApi::ping);

                // Server routes
                get("server", ServerApi::serverGet, Collections.singleton(Roles.ADMIN));
                get("worlds", ServerApi::worldsGet);
                get("worlds/:world", ServerApi::worldGet);

                // Communication
                post("broadcast", ServerApi::broadcastPost);

                // Player routes
                get("players", PlayerApi::playersGet);

                // Whitelist routes
                get("whitelist", ServerApi::whitelistGet);
                post("whitelist", ServerApi::whitelistPost);
            });
        });

        // Default fallthrough. Just give them a 404.
        app.get("*", ctx -> {
            throw new NotFoundResponse();
        });

        // Put the original class loader back where it was.
        Thread.currentThread().setContextClassLoader(classLoader);

    }
}
