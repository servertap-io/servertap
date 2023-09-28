package io.servertap.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.bukkit.Bukkit;

public class UselessAuthProvider implements RequestAuthProvider {

    private static final java.util.logging.Logger log = Bukkit.getLogger();

    @Override
    public void authenticateRequest(Handler handler, Context ctx) throws Exception {
        // do nothing, just allow the request lol!
        log.info("USELESSLY HANDLING A REQUEST!");
        handler.handle(ctx);
    }

}
