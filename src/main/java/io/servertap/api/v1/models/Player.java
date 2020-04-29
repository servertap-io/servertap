package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

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
    private Double balance =null;



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
}
