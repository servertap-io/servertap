package io.servertap.api.v1.websockets.models;

import java.util.Objects;

public class SocketID {
    private final String id;

    public SocketID(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SocketID socketID = (SocketID) object;
        return Objects.equals(id, socketID.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
