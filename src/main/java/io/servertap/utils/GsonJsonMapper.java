package io.servertap.utils;

import org.jetbrains.annotations.NotNull;

public class GsonJsonMapper implements io.javalin.plugin.json.JsonMapper {
    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj) {
        return GsonSingleton.getInstance().toJson(obj);
    }
}
