package io.servertap.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public interface RequestAuthProvider {

    void authenticateRequest(Handler handler, Context ctx) throws Exception;

}
