package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

import java.util.Set;

public class Objective {
    @Expose
    private String name;

    @Expose
    private String displayName;

    @Expose
    private String displaySlot;

    @Expose
    private String criterion;

    @Expose
    private Set<Score> scores;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    public String getDisplaySlot() {
        return displaySlot;
    }

    public void setDisplaySlot(String displaySlot) {
        this.displaySlot = displaySlot;
    }
}
