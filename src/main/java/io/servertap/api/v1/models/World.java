package io.servertap.api.v1.models;

import java.math.BigDecimal;

/**
 * A Minecraft world
 */
public class World {
    private String name = null;

    private String uuid = null;

    private BigDecimal time = null;

    private Boolean storm = null;

    private Boolean thundering = null;

    private Boolean generateStructures = null;

    private Boolean allowAnimals = null;

    private Boolean allowMonsters = null;

    private Integer difficulty = null;

    private Integer environment = null;

    private BigDecimal seed = null;

    public World name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The name of the world
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public World time(BigDecimal time) {
        this.time = time;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * The time of day
     *
     * @return time
     **/
    public BigDecimal getTime() {
        return time;
    }

    public void setTime(BigDecimal time) {
        this.time = time;
    }

    public World storm(Boolean storm) {
        this.storm = storm;
        return this;
    }

    /**
     * True if there is currently a storm
     *
     * @return storm
     **/
    public Boolean isStorm() {
        return storm;
    }

    public void setStorm(Boolean storm) {
        this.storm = storm;
    }

    public World thundering(Boolean thundering) {
        this.thundering = thundering;
        return this;
    }

    /**
     * True if it is currently thundering
     *
     * @return thundering
     **/
    public Boolean isThundering() {
        return thundering;
    }

    public void setThundering(Boolean thundering) {
        this.thundering = thundering;
    }

    public World generateStructures(Boolean generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }

    /**
     * True if the world can generate structures
     *
     * @return generateStructures
     **/
    public Boolean isGenerateStructures() {
        return generateStructures;
    }

    public void setGenerateStructures(Boolean generateStructures) {
        this.generateStructures = generateStructures;
    }

    public World allowAnimals(Boolean allowAnimals) {
        this.allowAnimals = allowAnimals;
        return this;
    }

    /**
     * True if animals can spawn
     *
     * @return allowAnimals
     **/
    public Boolean isAllowAnimals() {
        return allowAnimals;
    }

    public void setAllowAnimals(Boolean allowAnimals) {
        this.allowAnimals = allowAnimals;
    }

    public World allowMonsters(Boolean allowMonsters) {
        this.allowMonsters = allowMonsters;
        return this;
    }

    /**
     * True if monsters can spawn
     *
     * @return allowMonsters
     **/
    public Boolean isAllowMonsters() {
        return allowMonsters;
    }

    public void setAllowMonsters(Boolean allowMonsters) {
        this.allowMonsters = allowMonsters;
    }

    public World difficulty(Integer difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    /**
     * Peaceful (0), Easy (1), Normal (2), Hard (3)
     *
     * @return difficulty
     **/
    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public World environment(Integer environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Overworld (0), Nether (-1), End (1)
     *
     * @return environment
     **/
    public Integer getEnvironment() {
        return environment;
    }

    public void setEnvironment(Integer environment) {
        this.environment = environment;
    }

    public World seed(BigDecimal seed) {
        this.seed = seed;
        return this;
    }

    /**
     * The world seed
     *
     * @return seed
     **/
    public BigDecimal getSeed() {
        return seed;
    }

    public void setSeed(BigDecimal seed) {
        this.seed = seed;
    }

}
