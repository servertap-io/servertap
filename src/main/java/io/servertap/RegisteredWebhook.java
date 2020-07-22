package io.servertap;

import io.servertap.api.v1.models.events.WebhookEvent;

import java.util.List;

public class RegisteredWebhook {
	private String listenerUrl;
	private List<WebhookEvent.EventType> registeredEvents;

	public RegisteredWebhook(String listenerUrl, List<WebhookEvent.EventType> registeredEvents)
	{
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
}
