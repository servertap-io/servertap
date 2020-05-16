package io.servertap.api.v1;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.servertap.Constants;
import io.servertap.PluginEntrypoint;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class EconomyApi {
    public static void playerPay(Context ctx) {
        if(PluginEntrypoint.getEconomy() == null){
            throw new InternalServerErrorResponse(Constants.VAULT_MISSING);
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(ctx.formParam("uuid")));

        double amount = Double.parseDouble(ctx.formParam("amount"));
        
        if(amount <= 0){ // Make sure pay amount is more than zero
            throw new InternalServerErrorResponse(Constants.VAULT_GREATER_THAN_ZERO);
        }
        
        EconomyResponse response = PluginEntrypoint.getEconomy().depositPlayer(player, amount);
        if(response.type != ResponseType.SUCCESS){
            throw new InternalServerErrorResponse(response.errorMessage);
        }

        ctx.status(200).json("success");
    }

    public static void playerDebit(Context ctx) {
        if(PluginEntrypoint.getEconomy() == null){
            throw new InternalServerErrorResponse(Constants.VAULT_MISSING);
        }
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(ctx.formParam("uuid")));
        double amount = Double.parseDouble(ctx.formParam("amount"));

        if(amount <= 0){ // Make sure pay amount is more than zero
            throw new InternalServerErrorResponse(Constants.VAULT_GREATER_THAN_ZERO);
        }
        EconomyResponse response = PluginEntrypoint.getEconomy().withdrawPlayer(player, amount);
        if(response.type != ResponseType.SUCCESS){
            throw new InternalServerErrorResponse(response.errorMessage);
        }

        ctx.status(200).json("success");
    }
}