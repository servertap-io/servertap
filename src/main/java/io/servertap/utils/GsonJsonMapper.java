package io.servertap.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class GsonJsonMapper implements io.javalin.json.JsonMapper {
    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return GsonSingleton.getInstance().toJson(obj);
    }
}