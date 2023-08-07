package io.servertap.api.v1.websockets.models.events;

import io.servertap.utils.GsonSingleton;

public class ClientMessage {
    private String name;
    private String payload;

    public ClientMessage(String name, String payload) {
        this.name = name;
        this.payload = payload;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public <T> T getPayLoadAs(Class<T> clazz) {
        return GsonSingleton.getInstance().fromJson(payload, clazz);
    }
}
