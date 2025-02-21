package io.servertap.api.v1.models;

public class PermissionRequest {
    private String permission;
    private Boolean value;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
} 