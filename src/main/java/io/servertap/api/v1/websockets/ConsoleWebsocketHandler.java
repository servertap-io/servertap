package io.servertap.api.v1.websockets;

import com.google.gson.Gson;
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

public class ConsoleWebsocketHandler {

    private final static Map<String, WsContext> subscribers = new ConcurrentHashMap<>();

    public static void events(WsConfig ws) {
        ws.onConnect(ctx -> {
            subscribers.put(clientHash(ctx), ctx);

            for (ConsoleLine line : PluginEntrypoint.instance.consoleBuffer) {
                ctx.send(line);
            }
        });

        // Unsubscribe clients that disconnect
        ws.onClose(ctx -> {
            subscribers.remove(clientHash(ctx));
        });

        // Unsubscribe any subscribers that error out
        ws.onError(ctx -> {
            subscribers.remove(clientHash(ctx));
        });

        // Allow sending of commands
        ws.onMessage(ctx -> {
            String cmd = ctx.message().trim();

            if (!cmd.isEmpty()) {
                if (cmd.startsWith("/")) {
                    cmd = cmd.substring(1);
                }

                final String command = cmd;
                Plugin pluginInstance = PluginEntrypoint.instance;

                if (pluginInstance != null) {
                    // Run the command on the main thread
                    Bukkit.getScheduler().scheduleSyncDelayedTask(pluginInstance, () -> {

                        try {
                            CommandSender sender = Bukkit.getServer().getConsoleSender();
                            Bukkit.dispatchCommand(sender, command);
                        } catch (Exception e) {
                            // Just warn about the issue
                            Bukkit.getLogger().warning("Couldn't execute command over websocket");
                        }
                    });
                }
            }
        });
    }

    /**
     * Sends the specified message (as JSON) to all subscribed clients.
     *
     * @param message Object can be any Jackson/JSON serializable object
     */
    public static void broadcast(Object message) {
        subscribers.values().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            session.send(message);
        });
    }

    /**
     * Generate a unique hash for this subscriber using its connection properties
     *
     * @param ctx
     * @return String the hash
     */
    private static String clientHash(WsContext ctx) {
        return String.format("sub-%s-%s", ctx.host(), ctx.getSessionId());
    }
}
