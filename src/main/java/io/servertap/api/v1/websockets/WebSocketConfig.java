package io.servertap.api.v1.websockets;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.ConsoleLine;
import io.servertap.api.v1.models.Socket;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class WebSocketConfig {
    public static Socket configure(Socket socket, ServerTapMain main, Logger log) {
        socket.on("executeCommand", (msg) -> {
            String cmd = msg.getPayload().trim();
            log.info(cmd);
            if (!cmd.isEmpty()) {

                if (cmd.startsWith("/")) {
                    cmd = cmd.substring(1);
                }

                final String command = cmd;
                if (main != null) {
                    // Run the command on the main thread
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

                        try {
                            CommandSender sender = Bukkit.getServer().getConsoleSender();
                            Bukkit.dispatchCommand(sender, command);
                        } catch (Exception e) {
                            // Just warn about the issue
                            log.warning("Couldn't execute command over websocket");
                        }
                    });
                }
            }
        });
        socket.on("test1", (msg) -> {
            ConsoleLine cl = msg.getPayLoadAs(ConsoleLine.class);
            log.info(cl.getMessage());
        });
        return socket;
    }
}
