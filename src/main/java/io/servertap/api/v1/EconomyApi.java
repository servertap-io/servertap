package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.plugin.openapi.annotations.*;
import io.servertap.Constants;
import io.servertap.utils.EconomyWrapper;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.UUID;

public class EconomyApi {

    private enum TransactionType {
        PAY, DEBIT
    }

    @OpenApi(
            path = "/v1/economy",
            operationId = "getEconomyPluginInfo",
            method = HttpMethod.GET,
            summary = "Economy plugin information",
            tags = {"Economy"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = io.servertap.api.v1.models.Plugin.class)),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public static void getEconomyPluginInformation(Context ctx) {
        Plugin econPlugin;
        if (EconomyWrapper.getInstance().getEconomy() == null) {
            throw new HttpResponseException(424, Constants.VAULT_MISSING, new HashMap<>());
        } else {
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
            operationId = "payPlayer",
            method = HttpMethod.POST,
            summary = "Pay a player",
            description = "Deposits the provided amount into the player's Vault",
            tags = {"Economy"},
            headers = {
                    @OpenApiParam(name = "key")
            },
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
            operationId = "debitPlayer",
            summary = "Debit a player",
            description = "Withdraws the provided amount out of the player's Vault",
            tags = {"Economy"},
            headers = {
                    @OpenApiParam(name = "key")
            },
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
            throw new BadRequestResponse(Constants.VAULT_MISSING_PAY_PARAMS);
        }

        if (EconomyWrapper.getInstance().getEconomy() == null) {
            throw new HttpResponseException(424, Constants.VAULT_MISSING, new HashMap<>());
        }

        UUID playerUUID = ValidationUtils.safeUUID(ctx.formParam("uuid"));
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);

        double amount = Double.parseDouble(ctx.formParam("amount"));

        if (amount <= 0) { // Make sure pay amount is more than zero
            throw new BadRequestResponse(Constants.VAULT_GREATER_THAN_ZERO);
        }

        EconomyResponse response;

        if (action == TransactionType.PAY) {
            response = EconomyWrapper.getInstance().getEconomy().depositPlayer(player, amount);
        } else {
            response = EconomyWrapper.getInstance().getEconomy().withdrawPlayer(player, amount);
        }

        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            throw new InternalServerErrorResponse(response.errorMessage);
        }

        ctx.status(200).json("success");
    }
}
