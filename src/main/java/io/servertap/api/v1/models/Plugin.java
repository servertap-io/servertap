package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Plugin {

    @Expose
    private String name = null;

    @Expose
    private Boolean enabled = null;

    @Expose
    private String version = null;

    @Expose
    private List<String> authors = null;

    @Expose
    private String description = null;

    public Plugin name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Plugin enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Plugin version(String version) {
        this.version = version;
        return this;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}