package io.servertap.mojang.api.models;

public class NameChange {
    private String name;
    private long changedToAt;

    public NameChange(String name, long changedToAt) {
        setName(name);
        setChangedToAt(changedToAt);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getChangedToAt() {
        return changedToAt;
    }

    public void setChangedToAt(long changedToAt) {
        this.changedToAt = changedToAt;
    }
}
