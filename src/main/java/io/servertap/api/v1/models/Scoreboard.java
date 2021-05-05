package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

import java.util.Set;

public class Scoreboard {
    @Expose
    private Set<String> objectives;

    @Expose
    private Set<String> entries;

    public Set<String> getObjectives() {
        return objectives;
    }

    public void setObjectives(Set<String> objectives) {
        this.objectives = objectives;
    }

    public Set<String> getEntries() {
        return entries;
    }

    public void setEntries(Set<String> entries) {
        this.entries = entries;
    }
}
