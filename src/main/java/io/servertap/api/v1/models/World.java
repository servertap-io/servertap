package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World.Environment;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A Minecraft world
 */
public class World {
    @Expose
    private String name = null;

    @Expose
    private String uuid = null;

    @Expose
    private BigDecimal time = null;

    @Expose
    private Boolean storm = null;

    @Expose
    private Boolean thundering = null;

    @Expose
    private Boolean generateStructures = null;

    @Expose
    private Boolean allowAnimals = null;

    @Expose
    private Boolean allowMonsters = null;

    @Expose
    private Difficulty difficulty = null;

    @Expose
    private Environment environment = null;

    @Expose
    private BigDecimal seed = null;

    @Expose
    private Map<String, Object> gameRules = new HashMap<>();

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

    /**
     * Peaceful(PEACEFUL), Easy(EASY), Normal(NORMAL), Hard(HARD)
     *
     * @return difficulty
     **/
    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Overworld (NORMAL), Nether (NETHER), End (THE_END), Custom (CUSTOM)
     *
     * @return environment
     **/
    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
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

    public void setGameRule(String rule, Object value) {
        gameRules.put(rule, value);
    }

    public void setGameRules(Map gameRules) {
        this.gameRules = gameRules;
    }

    public Map getGameRules() {
        return gameRules;
    }

    public static World fromBukkitWorld(org.bukkit.World bukkitWorld) {
        World world = new World();

        world.setName(bukkitWorld.getName());
        world.setUuid(bukkitWorld.getUID().toString());
        world.setEnvironment(bukkitWorld.getEnvironment());
        world.setTime(BigDecimal.valueOf(bukkitWorld.getTime()));
        world.setAllowAnimals(bukkitWorld.getAllowAnimals());
        world.setAllowMonsters(bukkitWorld.getAllowMonsters());
        world.setGenerateStructures(bukkitWorld.canGenerateStructures());
        world.setDifficulty(bukkitWorld.getDifficulty());
        world.setSeed(BigDecimal.valueOf(bukkitWorld.getSeed()));
        world.setStorm(bukkitWorld.hasStorm());
        world.setThundering(bukkitWorld.isThundering());
        Arrays.stream(bukkitWorld.getGameRules()).forEach((rule) -> world.setGameRule(rule, bukkitWorld.getGameRuleValue(GameRule.getByName(rule))));

        return world;
    }
}
