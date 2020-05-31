package io.servertap.api.v1;

import java.util.ArrayList;
import java.util.UUID;

import io.javalin.plugin.openapi.annotations.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.servertap.Constants;
import io.servertap.PluginEntrypoint;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyApi {

    private enum TransactionType {
        PAY, DEBIT
    }

    @OpenApi(
            path = "/v1/economy",
            method = HttpMethod.GET,
            summary = "Economy plugin information",
            tags = {"Economy"},
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )

    public static void getEconomyPluginInformation(Context ctx){
        Plugin econPlugin;
        if (PluginEntrypoint.getEconomy() == null){
            throw new InternalServerErrorResponse(Constants.VAULT_MISSING);
        } else{
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                throw new InternalServerErrorResponse(Constants.ECONOMY_PLUGIN_MISSING);
            }
            econPlugin = rsp.getPlugin();
        }

        io.servertap.api.v1.models.Plugin plugin = new io.servertap.api.v1.models.Plugin();
        plugin.setName(econPlugin.getName());
        plugin.setEnabled(econPlugin.isEnabled());
        plugin.setVersion(econPlugin.getDescription().getVersion());

        ctx.json(plugin);
    }

    @OpenApi(
        path = "/v1/economy/pay",
        method = HttpMethod.POST,
        summary = "Pay a player",
        description = "Deposits the provided amount into the player's Vault",
        tags = {"Economy"},
        formParams = {
            @OpenApiFormParam(name = "uuid"),
            @OpenApiFormParam(name = "amount", type = Double.class)
        },
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
            @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
        }
    )
    public static void playerPay(Context ctx) {
        accountManager(ctx, TransactionType.PAY);
    }

    @OpenApi(
        path = "/v1/economy/debit",
        method = HttpMethod.POST,
        summary = "Debit a player",
        description = "Withdraws the provided amount out of the player's Vault",
        tags = {"Economy"},
        formParams = {
            @OpenApiFormParam(name = "uuid"),
            @OpenApiFormParam(name = "amount", type = Double.class)
        },
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
            @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
        }
    )
    public static void playerDebit(Context ctx) {
        accountManager(ctx, TransactionType.DEBIT);
    }

    private static void accountManager(Context ctx, TransactionType action) {
        if (ctx.formParam("uuid") == null || ctx.formParam("amount") == null) {
            throw new InternalServerErrorResponse(Constants.VAULT_MISSING_PAY_PARAMS);
        }

        if (PluginEntrypoint.getEconomy() == null) {
            throw new InternalServerErrorResponse(Constants.VAULT_MISSING);
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(ctx.formParam("uuid")));

        double amount = Double.parseDouble(ctx.formParam("amount"));

        if (amount <= 0) { // Make sure pay amount is more than zero
            throw new InternalServerErrorResponse(Constants.VAULT_GREATER_THAN_ZERO);
        }

        EconomyResponse response;

        if (action == TransactionType.PAY) {
            response = PluginEntrypoint.getEconomy().depositPlayer(player, amount);
        } else {
            response = PluginEntrypoint.getEconomy().withdrawPlayer(player, amount);
        }

        if (response.type != ResponseType.SUCCESS) {
            throw new InternalServerErrorResponse(response.errorMessage);
        }

        ctx.status(200).json("success");
    }
}