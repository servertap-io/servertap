package io.servertap.utils;

import io.servertap.ServerTapMain;
import net.md_5.bungee.api.ChatColor;

public class NormalizeMessage {
    public static String normalize(String message, ServerTapMain main) {
        try {
            if (!main.getConfig().getBoolean("normalizeMessages")) {
                return message;
            }
            return ChatColor.stripColor(message);
        } catch (Exception e) {
            return message;
        }
    }
}
