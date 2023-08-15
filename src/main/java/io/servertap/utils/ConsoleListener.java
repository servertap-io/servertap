package io.servertap.utils;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.ConsoleLine;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConsoleListener implements Filter {
    private final ServerTapMain plugin;
    private final List<Consumer<ConsoleLine>> listeners = new ArrayList<>();

    public ConsoleListener(ServerTapMain main) {
        this.plugin = main;
    }

    @Override
    public Result filter(LogEvent logEvent) {
        if (plugin.getMaxConsoleBufferSize() > 0 && plugin.getConsoleBuffer().size() >= plugin.getMaxConsoleBufferSize()) {
            plugin.getConsoleBuffer().remove(0);
        }

        ConsoleLine line = new ConsoleLine();

        line.setLevel(logEvent.getLevel().toString());
        line.setTimestampMillis(logEvent.getTimeMillis());
        line.setMessage(logEvent.getMessage().getFormattedMessage());
        line.setLoggerName(logEvent.getLoggerName());

        if (plugin.getMaxConsoleBufferSize() > 0) {
            plugin.getConsoleBuffer().add(line);
        }

        listeners.forEach(consoleLineConsumer -> consoleLineConsumer.accept(line));

        return Result.NEUTRAL;
    }

    public void addListener(Consumer<ConsoleLine> consoleLineConsumer) {
        listeners.add(consoleLineConsumer);
    }

    public void resetListeners() {
        listeners.clear();
    }

    @Override
    public Result getOnMismatch() {
        return null;
    }

    @Override
    public Result getOnMatch() {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable) {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return null;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }
}
