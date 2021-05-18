package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

/**
 * An overview of server health
 */
public class ServerHealth {

    /**
     * The amount of logical CPUs on this server
     */
    @Expose
    private Integer cpus = 0;

    /**
     * The amount of seconds this server has been running
     */
    @Expose
    private Long uptime = 0L;

    /**
     * The amount of memory in bytes that this server has
     */
    @Expose
    private Long totalMemory = 0L;

    /**
     * The maximum amount of memory in bytes that this server can use
     */
    @Expose
    private Long maxMemory = 0L;

    /**
     * The amount of memory in bytes that is available
     */
    @Expose
    private Long freeMemory = 0L;

    public Integer getCpus() {
        return cpus;
    }

    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    public Long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(Long freeMemory) {
        this.freeMemory = freeMemory;
    }
}
