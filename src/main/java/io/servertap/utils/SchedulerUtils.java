package io.servertap.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling scheduler operations in a way that's compatible with both Bukkit and Folia.
 */
public class SchedulerUtils {
    private static final Logger logger = Bukkit.getLogger();

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
        try {
            if (isFolia()) {
                // Use Folia's global region scheduler for server-wide tasks
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, (task) -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in repeating task (Folia)", e);
                    }
                }, delay, period);
            } else {
                // Use Bukkit's scheduler for traditional servers
                Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in repeating task (Bukkit)", e);
                    }
                }, delay, period);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to schedule repeating task", e);
        }
    }

    /**
     * Runs a task once that works on both Bukkit and Folia
     *
     * @param plugin The plugin instance
     * @param runnable The task to run
     */
    public static void runTask(Plugin plugin, Runnable runnable) {
        try {
            if (isFolia()) {
                // Use Folia's global region scheduler for server-wide tasks
                Bukkit.getGlobalRegionScheduler().run(plugin, (task) -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in task (Folia)", e);
                    }
                });
            } else {
                // Use Bukkit's scheduler for traditional servers
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in task (Bukkit)", e);
                    }
                });
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to schedule task", e);
        }
    }

    /**
     * Runs a task asynchronously that works on both Bukkit and Folia
     *
     * @param plugin The plugin instance
     * @param runnable The task to run
     */
    public static void runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        try {
            if (isFolia()) {
                // Use Folia's AsyncScheduler for async tasks
                Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in async task (Folia)", e);
                    }
                });
            } else {
                // Use Bukkit's scheduler for traditional servers
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in async task (Bukkit)", e);
                    }
                });
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to schedule async task", e);
        }
    }

    /**
     * Runs a task with a delay that works on both Bukkit and Folia
     *
     * @param plugin The plugin instance
     * @param runnable The task to run
     * @param delay The delay before execution in ticks
     */
    public static void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        try {
            if (isFolia()) {
                // Use Folia's global region scheduler for server-wide tasks
                Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in delayed task (Folia)", e);
                    }
                }, delay);
            } else {
                // Use Bukkit's scheduler for traditional servers
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error in delayed task (Bukkit)", e);
                    }
                }, delay);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to schedule delayed task", e);
        }
    }
}
