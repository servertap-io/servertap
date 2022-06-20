package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

/**
 * An offline player
 */
public class OfflinePlayer {
    @Expose
    private String uuid = null;

    @Expose
    private String name = null;

    @Expose
    private Boolean whitelisted = null;

    @Expose
    private Boolean banned = null;

    @Expose
    private Boolean op = null;

    @Expose
    private Double balance = null;

    @Expose
    private Long lastPlayed = null;

    public OfflinePlayer uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }


    /**
     * The players balance
     *
     * @return balance
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

    public OfflinePlayer name(String displayName) {
        this.name = displayName;
        return this;
    }

    /**
     * The Player's display name
     *
     * @return displayName
     **/
    public String getDisplayName() {
        return name;
    }

    public void setDisplayName(String displayName) {
        this.name = displayName;
    }


    public OfflinePlayer whitelisted(Boolean whitelisted) {
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

    public OfflinePlayer banned(Boolean banned) {
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

    public OfflinePlayer op(Boolean op) {
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

    public Long getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }
}
