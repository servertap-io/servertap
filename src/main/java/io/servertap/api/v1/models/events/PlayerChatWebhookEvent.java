package io.servertap.api.v1.models.events;

import com.google.gson.annotations.Expose;

public class PlayerChatWebhookEvent extends WebhookEvent {
	@Expose
	String playerName;

	@Expose
	String message;

	public PlayerChatWebhookEvent() {
		eventType = EventType.PlayerChat;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
