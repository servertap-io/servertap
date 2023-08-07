package io.servertap.api.v1.websockets.models.events;

public class ServerMessage {
    private String name;
    private Object payload;

    public ServerMessage(String name, Object payload) {
        this.name = name;
        this.payload = payload;
    }

    public String getName() {
        return name;
    }

    public Object getPayload() {
        return payload;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
