package io.servertap.commands;

import io.servertap.ServerTapMain;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class ServerTapCommand implements CommandExecutor, TabCompleter {

    private final ServerTapMain main;

    public ServerTapCommand(ServerTapMain main) {
        this.main = main;

        PluginCommand pluginCommand = main.getCommand("servertap");
        if (pluginCommand != null) {
            pluginCommand.setTabCompleter(this);
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!commandSender.hasPermission("servertap.admin")) {
            commandSender.sendMessage(String.format("%s[%sServerTap%s] %sYou do not have the permission to do that!", DARK_GRAY, BLUE, DARK_GRAY, AQUA));
            return false;
        }
        if (args.length == 1) {
            switch (args[0]) {
                case "reload":
                    main.reload();
                    commandSender.sendMessage(String.format("%s[%sServerTap%s] %sServerTap reloaded!", DARK_GRAY, BLUE, DARK_GRAY, AQUA));
                    break;
                case "info":
                    String version = main.getDescription().getVersion();
                    String website = main.getDescription().getWebsite();
                    String authors = String.join(", ", main.getDescription().getAuthors());
                    commandSender.sendMessage(String.format("%sServerTap Plugin Information:\n%sVersion: %s%s\n%sWebsite: %s%s\n%sAuthors: %s%s",
                            BLUE, BLUE, AQUA, version, BLUE, AQUA, website, BLUE, AQUA, authors));
                    break;
                default:
                    commandSender.sendMessage(String.format("%s[%sServerTap%s] %sUnknown Command.", DARK_GRAY, BLUE, DARK_GRAY, AQUA));
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!commandSender.hasPermission("servertap.admin")) {
            return null;
        }
        ArrayList<String> completions = new ArrayList<>();
        if (args.length == 0 || (args.length == 1 && args[0].length() == 0)) {
            completions.add("reload");
            completions.add("info");
        }
        return completions;
    }
}
