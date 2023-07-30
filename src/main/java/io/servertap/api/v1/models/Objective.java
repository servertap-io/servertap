package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashSet;
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

    public static Objective fromBukkitObjective(org.bukkit.scoreboard.Objective objective, ScoreboardManager scoreboardManager) {
        org.bukkit.scoreboard.Scoreboard gameScoreboard = scoreboardManager.getMainScoreboard();

        Objective o = new Objective();
        o.setCriterion(objective.getCriteria());
        o.setDisplayName(objective.getDisplayName());
        o.setName(objective.getName());

        o.setDisplaySlot("");
        if (objective.getDisplaySlot() != null) {
            o.setDisplaySlot(objective.getDisplaySlot().toString().toLowerCase());
        }

        Set<Score> scores = new HashSet<>();
        gameScoreboard.getEntries().forEach(entry -> {
            org.bukkit.scoreboard.Score score = objective.getScore(entry);

            if (score.isScoreSet()) {
                Score s = new Score();
                s.setEntry(entry);
                s.setValue(score.getScore());

                scores.add(s);
            }
        });
        o.setScores(scores);

        return o;
    }
}
