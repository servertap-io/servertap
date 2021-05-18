package io.servertap.api.v1.models;

public class Plugin {

    private String name = null;

    private Boolean enabled = null;

    private String version = null;

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
}