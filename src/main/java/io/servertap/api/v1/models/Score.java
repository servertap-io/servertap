package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

public class Score {
    @Expose
    private String entry;

    @Expose
    private int value;

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
