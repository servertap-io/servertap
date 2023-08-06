package io.servertap.utils;

import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class GsonJsonMapper implements JsonMapper {
    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return GsonSingleton.getInstance().toJson(obj);
    }
    @NotNull
    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type type) {
        return GsonSingleton.getInstance().fromJson(json, type);
    }
}