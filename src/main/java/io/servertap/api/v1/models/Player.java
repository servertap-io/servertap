package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;
import org.bukkit.GameMode;
import org.bukkit.World;

/**
 * An online player
 */
public class Player {
    @Expose
    private String uuid = null;

    @Expose
    private String displayName = null;

    @Expose
    private String address = null;

    @Expose
    private Integer port = null;

    @Expose
    private Float exhaustion = null;

    @Expose
    private Float exp = null;

    @Expose
    private Boolean whitelisted = null;

    @Expose
    private Boolean banned = null;

    @Expose
    private Boolean op = null;

    @Expose
    private Double balance = null;

    @Expose
    private Double[] location = null;

    @Expose
    private World.Environment dimension = null;

    @Expose
    private Double health = null;

    @Expose
    private Integer hunger = null;

    @Expose
    private Float saturation = null;

    @Expose
    private GameMode gamemode = null;

    @Expose
    private Long lastPlayed = null;

    public Player uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }


    /**
     * Current exhaustion level
     *
     * @return exhaustion
     **/
    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }


    /**
     * The Player's UUID
     *
     * @return uuid
     **/
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Player displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * The Player's display name
     *
     * @return displayName
     **/
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Player address(String address) {
        this.address = address;
        return this;
    }

    /**
     * The address the Player is connected from (usually an IP)
     *
     * @return address
     **/
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Player port(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * The port the Player is connected from
     *
     * @return port
     **/
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Player exhaustion(Float exhaustion) {
        this.exhaustion = exhaustion;
        return this;
    }

    /**
     * Current exhaustion level
     *
     * @return exhaustion
     **/
    public Float getExhaustion() {
        return exhaustion;
    }

    public void setExhaustion(Float exhaustion) {
        this.exhaustion = exhaustion;
    }

    public Player exp(Float exp) {
        this.exp = exp;
        return this;
    }

    /**
     * Current exp towards next level
     *
     * @return exp
     **/
    public Float getExp() {
        return exp;
    }

    public void setExp(Float exp) {
        this.exp = exp;
    }

    public Player whitelisted(Boolean whitelisted) {
        this.whitelisted = whitelisted;
        return this;
    }

    /**
     * True if this Player is on the server's whitelist
     *
     * @return whitelisted
     **/
    public Boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(Boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public Player banned(Boolean banned) {
        this.banned = banned;
        return this;
    }

    /**
     * True if this Player is banned
     *
     * @return banned
     **/
    public Boolean isBanned() {
        return banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public Player op(Boolean op) {
        this.op = op;
        return this;
    }

    /**
     * True if this Player is OP
     *
     * @return op
     **/
    public Boolean isOp() {
        return op;
    }

    public void setOp(Boolean op) {
        this.op = op;
    }

    /**
     * Location of the player as an array of x,y,z
     *
     * @return location
     */
    public Double[] getLocation() {
        return location;
    }

    public void setLocation(Double[] location) {
        this.location = location;
    }

    /**
     * The dimension the player is currently in. Can be NORMAL, NETHER, THE_END, or CUSTOM
     *
     * @return world
     */
    public World.Environment getDimension() {
        return dimension;
    }

    public void setDimension(World.Environment dimension) {
        this.dimension = dimension;
    }

    /**
     * The health of the player as a Double
     *
     * @return health
     */
    public Double getHealth() {
        return health;
    }

    public void setHealth(Double health) {
        this.health = health;
    }

    /**
     * The hunger of the player as an Integer
     *
     * @return hunger
     */
    public Integer getHunger() {
        return hunger;
    }

    public void setHunger(Integer hunger) {
        this.hunger = hunger;
    }

    /**
     * The saturation of the player as a Float
     *
     * @return saturation
     */
    public Float getSaturation() {
        return saturation;
    }

    public void setSaturation(Float saturation) {
        this.saturation = saturation;
    }

    /**
     * The gamemode of the player, can be SURVIVAL, CREATIVE, ADVENTURE, or SPECTATOR
     *
     * @return gamemode
     */
    public GameMode getGamemode() {
        return gamemode;
    }

    public void setGamemode(GameMode gamemode) {
        this.gamemode = gamemode;
    }

    public Long getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }
}
