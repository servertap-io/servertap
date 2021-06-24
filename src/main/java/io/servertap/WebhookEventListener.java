package io.servertap;

import com.google.gson.Gson;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.Player;
import io.servertap.api.v1.models.events.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.*;

public class WebhookEventListener implements Listener {
    private List<RegisteredWebhook> registeredWebhooks;
    private final Plugin plugin;

    public WebhookEventListener(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration bukkitConfig = plugin.getConfig();
        Logger logger = Bukkit.getLogger();
        String pluginName = plugin.getDescription().getName();

        registeredWebhooks = new ArrayList<>();

        //Register webhooks from config
        if (bukkitConfig.isSet("webhooks")) {
            Set<String> webhookNames = bukkitConfig.getConfigurationSection("webhooks").getKeys(false);

            for (String webhookName : webhookNames) {
                String configPath = "webhooks." + webhookName + ".";

                //Check for listener parameter
                if (!bukkitConfig.isSet(configPath + "listener")) {
                    logger.warning(String.format("[%s] Error: webhook '%s' doesn't have 'listener' set", pluginName, webhookName));
                    continue;
                }

                //Check for events parameter
                if (!bukkitConfig.isSet(configPath + "events")) {
                    logger.warning(String.format("[%s] Error: webhook '%s' doesn't have 'events' set", pluginName, webhookName));
                    continue;
                }

                //Validate listener url
                String listenerUrl = bukkitConfig.getString(configPath + "listener");
                try {
                    new URL(listenerUrl);
                } catch (MalformedURLException ex) {
                    logger.warning(String.format("[%s] Error: webhook '%s' url is invalid", pluginName, webhookName));
                    continue;
                }

                //Add events
                List<WebhookEvent.EventType> events = new ArrayList<>();
                List<String> configEvents = bukkitConfig.getStringList(configPath + "events");

                //If the events path can't be interpreted as a list, try as a single string
                if (configEvents.size() == 0) {
                    String singleEvent = bukkitConfig.getString(configPath + "events");

                    if (singleEvent != null) {
                        configEvents.add(singleEvent);
                    } else {
                        logger.warning(String.format("[%s] Error: webhook \"%s\" doesn't register any events", pluginName, webhookName));
                        continue;
                    }
                }

                for (String event : configEvents) {
                    try {
                        WebhookEvent.EventType eventType = WebhookEvent.EventType.valueOf(event);

                        if (events.contains(eventType)) {
                            logger.warning(String.format("[%s] Warning: webhook '%s' registers duplicate event '%s'", pluginName, webhookName, event));
                            continue;
                        }

                        events.add(eventType);
                    } catch (IllegalArgumentException ex) {
                        logger.warning(String.format("[%s] Warning: webhook '%s' attempts to register invalid event '%s'", pluginName, webhookName, event));
                    }
                }

                registeredWebhooks.add(new RegisteredWebhook(listenerUrl, events));
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerChatWebhookEvent eventModel = new PlayerChatWebhookEvent();

        eventModel.setMessage(normalizeMessage(event.getMessage()));
        eventModel.setPlayerName(event.getPlayer().getDisplayName());

        for (RegisteredWebhook webhook : registeredWebhooks) {
            List<WebhookEvent.EventType> registeredEvents = webhook.getRegisteredEvents();

            if (!registeredEvents.contains(WebhookEvent.EventType.PlayerChat)) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new PostRequestTask(eventModel, webhook.getListenerUrl()));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerDeathWebhookEvent eventModel = new PlayerDeathWebhookEvent();

        Player player = fromBukkitPlayer(event.getEntity());
        List<ItemStack> drops = new ArrayList<>();
        event.getDrops().forEach(itemStack -> drops.add(fromBukkitItemStack(itemStack)));

        eventModel.setPlayer(player);
        eventModel.setDrops(drops);
        eventModel.setDeathMessage(normalizeMessage(event.getDeathMessage()));

        for (RegisteredWebhook webhook : registeredWebhooks) {
            List<WebhookEvent.EventType> registeredEvents = webhook.getRegisteredEvents();

            if (!registeredEvents.contains(WebhookEvent.EventType.PlayerDeath)) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new PostRequestTask(eventModel, webhook.getListenerUrl()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerJoinWebhookEvent eventModel = new PlayerJoinWebhookEvent();

        Player player = fromBukkitPlayer(event.getPlayer());

        eventModel.setPlayer(player);
        eventModel.setJoinMessage(normalizeMessage(event.getJoinMessage()));

        for (RegisteredWebhook webhook : registeredWebhooks) {
            List<WebhookEvent.EventType> registeredEvents = webhook.getRegisteredEvents();

            if (!registeredEvents.contains(WebhookEvent.EventType.PlayerJoin)) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new PostRequestTask(eventModel, webhook.getListenerUrl()));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerQuitWebhookEvent eventModel = new PlayerQuitWebhookEvent();

        Player player = fromBukkitPlayer(event.getPlayer());

        eventModel.setPlayer(player);
        eventModel.setQuitMessage(normalizeMessage(event.getQuitMessage()));

        for (RegisteredWebhook webhook : registeredWebhooks) {
            List<WebhookEvent.EventType> registeredEvents = webhook.getRegisteredEvents();

            if (!registeredEvents.contains(WebhookEvent.EventType.PlayerQuit)) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new PostRequestTask(eventModel, webhook.getListenerUrl()));
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        PlayerKickWebhookEvent eventModel = new PlayerKickWebhookEvent();

        Player player = fromBukkitPlayer(event.getPlayer());

        eventModel.setPlayer(player);
        eventModel.setReason(normalizeMessage(event.getReason()));

        for (RegisteredWebhook webhook : registeredWebhooks) {
            List<WebhookEvent.EventType> registeredEvents = webhook.getRegisteredEvents();

            if (!registeredEvents.contains(WebhookEvent.EventType.PlayerKick)) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new PostRequestTask(eventModel, webhook.getListenerUrl()));
        }
    }

    private ItemStack fromBukkitItemStack(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack i = new ItemStack();
        i.setId("minecraft:" + itemStack.getType().toString().toLowerCase());
        i.setCount(itemStack.getAmount());
        i.setSlot(-1);

        return i;
    }

    private Player fromBukkitPlayer(org.bukkit.entity.Player player) {
        Player p = new Player();

        if (PluginEntrypoint.getEconomy() != null) {
            p.setBalance(PluginEntrypoint.getEconomy().getBalance(player));
        }

        p.setUuid(player.getUniqueId().toString());
        p.setDisplayName(player.getDisplayName());

        p.setAddress(player.getAddress().getHostName());
        p.setPort(player.getAddress().getPort());

        p.setExhaustion(player.getExhaustion());
        p.setExp(player.getExp());

        p.setWhitelisted(player.isWhitelisted());
        p.setBanned(player.isBanned());
        p.setOp(player.isOp());

        return p;
    }

    private String normalizeMessage(String message) {
        try {
            if(!this.plugin.getConfig().getBoolean("normalizeMessages")){
                return message;
            }
            Pattern chatCodePattern = Pattern.compile("§(4|c|6|e|2|a|b|3|1|9|d|5|f|7|8|l|n|o|k|m|r)", Pattern.CASE_INSENSITIVE);
            Matcher chatCodeMatcher = chatCodePattern.matcher(message);
            return chatCodeMatcher.replaceAll("");
                    
        } catch (Exception e) {
            return message;
        }
    }
    private static class PostRequestTask implements Runnable {
        private final WebhookEvent webhookEvent;
        private final String listener;

        public PostRequestTask(WebhookEvent webhookEvent, String listener) {
            this.webhookEvent = webhookEvent;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                Gson gson = new Gson();
                String jsonContent = gson.toJson(webhookEvent);
                byte[] output = jsonContent.getBytes(StandardCharsets.UTF_8);

                URL url = new URL(listener);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setFixedLengthStreamingMode(output.length);
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                http.connect();
                try (OutputStream os = http.getOutputStream()) {
                    os.write(output);
                }
            } catch (MalformedURLException ignored) {
                //This branch should never be reached, since all listeners are validated in the constructor
            } catch (IOException ignored) {

            }
        }
    }
}
