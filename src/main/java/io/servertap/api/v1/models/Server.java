package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

/**
 * A Bukkit/Spigot/Paper server
 */
public class Server {
    @Expose
    private String name = null;

    public Server name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The name of the server
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
