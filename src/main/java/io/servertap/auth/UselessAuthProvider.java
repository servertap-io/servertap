package io.servertap.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class UselessAuthProvider implements RequestAuthProvider {

    @Override
    public void authenticateRequest(Handler handler, Context ctx) throws Exception {
        // do nothing, just allow the request lol!
        handler.handle(ctx);
    }

}
