package io.servertap.api.v1;

import io.javalin.http.Context;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.Advancement;
import io.servertap.api.v1.models.Player;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.logging.Logger;

public class AdvancementsApi {
    public AdvancementsApi() {}

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
        final ArrayList<Advancement> advancements = new ArrayList<>();

        Bukkit.advancementIterator().forEachRemaining(advancement -> {
            Advancement a = new Advancement();
            a.setName(advancement.getKey().getKey());
            a.setCriteria(advancement.getCriteria().stream().toList());
            advancements.add(a);
        });

        ctx.json(advancements);
    }

}