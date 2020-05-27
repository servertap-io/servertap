package io.servertap.api.v1.websockets;

import io.javalin.http.Context;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class WebsocketHandler {

    private final static Map<String, WsContext> subscribers = new ConcurrentHashMap<>();

    public static void events(WsHandler ws) {
        ws.onConnect(ctx -> {
            subscribers.put(clientHash(ctx), ctx);
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
                Plugin pluginInstance = Bukkit.getPluginManager().getPlugin("ServerTap");

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
