package io.servertap.api.v1;

import io.javalin.http.Context;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.servertap.api.v1.models.Advancement;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class AdvancementsApi {
    @OpenApi(
            path = "/v1/advancements",
            summary = "Gets all server advancements",
            tags = {"Advancement"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Advancement.class))
            }
    )
    public void getAdvancements(Context ctx) {
        ctx.json(getAdvancements());
    }

    public ArrayList<Advancement> getAdvancements() {
        final ArrayList<Advancement> advancements = new ArrayList<>();

        Bukkit.advancementIterator().forEachRemaining(advancement -> advancements.add(Advancement.fromBukkitAdvancement(advancement)));

        return advancements;
    }

}