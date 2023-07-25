package io.servertap.webhooks;

import io.servertap.webhooks.models.events.WebhookEvent;
import org.bukkit.configuration.file.FileConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Webhook {
    private String listenerUrl;
    private List<WebhookEvent.EventType> registeredEvents;

    public Webhook(String listenerUrl, List<WebhookEvent.EventType> registeredEvents) {
        setListenerUrl(listenerUrl);
        setRegisteredEvents(registeredEvents);
    }

    public String getListenerUrl() {
        return listenerUrl;
    }

    public void setListenerUrl(String listenerUrl) {
        this.listenerUrl = listenerUrl;
    }

    public List<WebhookEvent.EventType> getRegisteredEvents() {
        return registeredEvents;
    }

    public void setRegisteredEvents(List<WebhookEvent.EventType> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    public static Optional<Webhook> getWebhookFromConfig(FileConfiguration bukkitConfig, String webhookName, String configPath, Logger log) {
        // Check for listener parameter and validate it
        String listenerUrl = bukkitConfig.getString(configPath + "listener");
        if (listenerUrl == null) {
            log.warning(String.format("[ServerTap] Warning: webhook '%s' doesn't have 'listener' set", webhookName));
            return Optional.empty();
        }

        try {
            new URL(listenerUrl);
        } catch (MalformedURLException ex) {
            log.warning(String.format("[ServerTap] Warning: webhook '%s' url is invalid", webhookName));
            return Optional.empty();
        }

        // Check for events parameter
        if (!bukkitConfig.isSet(configPath + "events")) {
            log.warning(String.format("[ServerTap] Warning: webhook '%s' doesn't have 'events' set", webhookName));
            return Optional.empty();
        }

        List<WebhookEvent.EventType> events = getWebhookEvents(webhookName, configPath, bukkitConfig, log);
        return events.isEmpty() ? Optional.empty() : Optional.of(new Webhook(listenerUrl, events));
    }

    private static List<WebhookEvent.EventType> getWebhookEvents(String webhookName, String configPath, FileConfiguration bukkitConfig, Logger log) {
        List<WebhookEvent.EventType> events = new ArrayList<>();
        List<String> configEvents = bukkitConfig.getStringList(configPath + "events");

        // If the events path can't be interpreted as a list, try as a single string
        if (configEvents.isEmpty()) {
            String singleEvent = bukkitConfig.getString(configPath + "events");

            if (singleEvent != null) {
                configEvents.add(singleEvent);
            } else {
                log.warning(String.format("[ServerTap] Warning: webhook \"%s\" doesn't register any events", webhookName));
                return events;
            }
        }

        for (String event : configEvents) {
            try {
                WebhookEvent.EventType eventType = WebhookEvent.EventType.valueOf(event);

                if (events.contains(eventType)) {
                    log.warning(String.format("[ServerTap] Warning: webhook '%s' registers duplicate event '%s'", webhookName, event));
                    continue;
                }

                events.add(eventType);
            } catch (IllegalArgumentException ex) {
                log.warning(String.format("[ServerTap] Warning: webhook '%s' attempts to register invalid event '%s'", webhookName, event));
            }
        }
        return events;
    }
}