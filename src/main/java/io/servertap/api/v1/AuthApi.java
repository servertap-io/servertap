package io.servertap.api.v1;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.servertap.WebServer;

public class AuthApi {

    @OpenApi(
            path = "/v1/login",
            methods = {HttpMethod.POST},
            summary = "Log in the user and return a JWT access token",
            tags = {"Login"},
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "username", type = "string"),
                                            @OpenApiContentProperty(name = "password", type = "string", format = "password")
                                    }
                            )
                    }
            ),
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = io.servertap.api.v1.models.Login.class)),
                    @OpenApiResponse(status = "401", content = @OpenApiContent(type = "application/json"))
            }
    )

    public void login(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        if (!WebServer.validateCredentials(username, password)) {
            ctx.status(401).json("Invalid username or password");
        } else {
            ctx.status(200).json(WebServer.generateJWT(username));
        }
    }
}
