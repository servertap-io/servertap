package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.utils.pluginwrappers.EconomyWrapper;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.UUID;

public class EconomyApi {
    private final org.bukkit.Server bukkitServer = Bukkit.getServer();
    private final EconomyWrapper economy;

    public EconomyApi(EconomyWrapper economyWrapper) {
        this.economy = economyWrapper;
    }

    private enum TransactionType {
        PAY, DEBIT
    }

    @OpenApi(
            path = "/v1/economy",
            methods = {HttpMethod.GET},
            summary = "Economy plugin information",
            tags = {"Economy"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void getEconomyPluginInformation(Context ctx) {
        Plugin econPlugin;
        if (!economy.isAvailable()) {
            throw new HttpResponseException(424, Constants.VAULT_MISSING, new HashMap<>());
        } else {
            RegisteredServiceProvider<Economy> rsp = bukkitServer.getServicesManager().getRegistration(Economy.class);
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
            methods = {HttpMethod.POST},
            summary = "Pay a player",
            description = "Deposits the provided amount into the player's Vault",
            tags = {"Economy"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "uuid", type = "string"),
                                            @OpenApiContentProperty(name = "amount", type = "double")
                                    }
                            )
                    }
            ),
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void playerPay(Context ctx) {
        accountManager(ctx, TransactionType.PAY);
    }

    @OpenApi(
            path = "/v1/economy/debit",
            methods = {HttpMethod.POST},
            summary = "Debit a player",
            description = "Withdraws the provided amount out of the player's Vault",
            tags = {"Economy"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "uuid", type = "string"),
                                            @OpenApiContentProperty(name = "amount", type = "double")
                                    }
                            )
                    }
            ),
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void playerDebit(Context ctx) {
        accountManager(ctx, TransactionType.DEBIT);
    }

    private void accountManager(Context ctx, TransactionType action) {
        String uuid = ctx.formParam("uuid");
        String amount = ctx.formParam("amount");

        if (uuid == null || amount == null) {
            throw new BadRequestResponse(Constants.VAULT_MISSING_PAY_PARAMS);
        }

        if (!economy.isAvailable()) {
            throw new HttpResponseException(424, Constants.VAULT_MISSING, new HashMap<>());
        }

        UUID playerUUID = ValidationUtils.safeUUID(uuid);
        if (playerUUID == null) {
            throw new BadRequestResponse(Constants.INVALID_UUID);
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);

        double amountNum = Double.parseDouble(amount);

        if (amountNum <= 0) { // Make sure pay amount is more than zero
            throw new BadRequestResponse(Constants.VAULT_GREATER_THAN_ZERO);
        }

        EconomyResponse response;

        if (action == TransactionType.PAY) {
            response = economy.depositPlayer(player, amountNum);
        } else {
            response = economy.withdrawPlayer(player, amountNum);
        }

        if (response.type != EconomyResponse.ResponseType.SUCCESS) {
            throw new InternalServerErrorResponse(response.errorMessage);
        }

        ctx.status(200).json("success");
    }
}