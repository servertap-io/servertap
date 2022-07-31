package io.servertap.api.v1.websockets;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import io.servertap.Constants;
import io.servertap.PluginEntrypoint;
import io.servertap.api.v1.PlayerApi;
import io.servertap.api.v1.ValidationUtils;
import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.OfflinePlayer;
import io.servertap.api.v1.models.Player;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketHandler {

    private static final Gson gson = new Gson();

    public static void events(WsConfig ws) {
        ws.onMessage(ctx -> {
            String cmd = ctx.message().trim();

            if (!cmd.isEmpty()) {
                String[] args = cmd.split(" ");
                try {
                    switch (args[0]) {
                        case "getPlayers" -> {
                            List<Player> players = PlayerApi.playersGet();
                            sendJsonMessageWithType(ctx, players, "getPlayers");
                        }
                        case "getPlayer" -> {
                            if (args.length < 2) throw new IllegalArgumentException(Constants.PLAYER_UUID_MISSING);
                            UUID uuid = ValidationUtils.safeUUID(args[1]);
                            if (uuid == null) throw new IllegalArgumentException(Constants.INVALID_UUID);
                            Player p = PlayerApi.playerGet(uuid);
                            sendJsonMessageWithType(ctx, p, "getPlayer");
                        }
                        case "getAllPlayers" -> {
                            List<OfflinePlayer> offlinePlayers = PlayerApi.offlinePlayersGet();
                            sendJsonMessageWithType(ctx, offlinePlayers, "getAllPlayers");
                        }
                        case "getPlayerInventory" -> {
                            if (args.length < 3) throw new IllegalArgumentException(Constants.PLAYER_MISSING_PARAMS);
                            try {
                                ArrayList<ItemStack> inv = PlayerApi.getPlayerInv(ValidationUtils.safeUUID(args[1]), ValidationUtils.safeUUID(args[2]));
                                sendJsonMessageWithType(ctx, inv, "getPlayerInventory");
                            } catch (IOException e) {
                                throw new IllegalArgumentException(Constants.INTERNAL_SERVER_ERROR);
                            }
                        }
                        default -> throw new IllegalArgumentException(Constants.UNKNOWN_WEBSOCKET_COMMAND);
                    }
                } catch (IllegalArgumentException e) {
                    sendJsonErrorWithType(ctx, e.getMessage(), args[0]);
                }
            }
        });
    }

    private static void sendJsonMessageWithType(WsContext cxt, Object json, String type) {
        cxt.send(gson.toJson(new JsonMessageWithType(json, type)));
    }
    private static void sendJsonErrorWithType(WsContext cxt, String error, String type) {
        cxt.send(gson.toJson(new JsonErrorWithType(error, type)));
    }

    public static class JsonMessageWithType {
        @Expose
        Object data;

        @Expose
        String type;

        public JsonMessageWithType(Object data, String type) {
            this.data = data;
            this.type = type;
        }
    }
    public static class JsonErrorWithType {
        @Expose
        String error;

        @Expose
        String type;

        public JsonErrorWithType(String error, String type) {
            this.error = error;
            this.type = type;
        }
    }

}
