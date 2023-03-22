package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;
import io.servertap.Constants;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PAPIApi {

    @OpenApi(
            path = "/v1/placeholders/replace",
            operationId = "replacePlaceholders",
            method = HttpMethod.POST,
            summary = "Process a string using PlaceholderAPI",
            description = "Process a string using PlaceholderAPI",
            tags = {"PlaceholderAPI"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            formParams = {
                    @OpenApiFormParam(name = "message", required = true),
                    @OpenApiFormParam(name = "uuid"),
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public static void replacePlaceholders(Context ctx) {
        OfflinePlayer player = null;

        String passedUuid = ctx.formParam("uuid");
        if (passedUuid != null && !passedUuid.isEmpty()) {
            UUID playerUUID = ValidationUtils.safeUUID(passedUuid);
            if (playerUUID == null) {
                throw new BadRequestResponse(Constants.INVALID_UUID);
            }

            player = Bukkit.getOfflinePlayer(playerUUID);
        }

        String passedMessage = ctx.formParam("message");
        if (passedMessage == null || passedMessage.isEmpty()) {
            throw new BadRequestResponse(Constants.PAPI_MESSAGE_MISSING);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            passedMessage = PlaceholderAPI.setPlaceholders(player, passedMessage);
        }

        ctx.status(200).json(passedMessage);
    }
}
