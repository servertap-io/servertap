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
    private String website = null;

    @Expose
    private List<String> authors = null;

    @Expose
    private List<String> depends = null;

    @Expose
    private List<String> softDepends = null;

    @Expose
    private String apiVersion = null;

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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<String> getDepends() {
        return depends;
    }

    public void setDepends(List<String> depends) {
        this.depends = depends;
    }

    public List<String> getSoftDepends() {
        return softDepends;
    }

    public void setSoftDepends(List<String> softDepends) {
        this.softDepends = softDepends;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
