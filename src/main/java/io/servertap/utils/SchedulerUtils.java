package io.servertap.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Utility class for handling scheduler operations in a way that's compatible with both Bukkit and Folia.
 */
public class SchedulerUtils {

    /**
     * Checks if the server is running Folia
     * 
     * @return true if the server is running Folia, false otherwise
     */
    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Runs a repeating task that works on both Bukkit and Folia
     * 
     * @param plugin The plugin instance
     * @param runnable The task to run
     * @param delay The delay before first execution in ticks
     * @param period The period between executions in ticks
     */
    public static void runRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        if (isFolia()) {
            // Use Folia's global region scheduler for server-wide tasks
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> runnable.run(), delay, period);
        } else {
            // Use Bukkit's scheduler for traditional servers
            Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
        }
    }

    /**
     * Runs a task once that works on both Bukkit and Folia
     * 
     * @param plugin The plugin instance
     * @param runnable The task to run
     */
    public static void runTask(Plugin plugin, Runnable runnable) {
        if (isFolia()) {
            // Use Folia's global region scheduler for server-wide tasks
            Bukkit.getGlobalRegionScheduler().run(plugin, (task) -> runnable.run());
        } else {
            // Use Bukkit's scheduler for traditional servers
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
}
