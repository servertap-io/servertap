package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

public class ConsoleLine {

    @Expose
    private String message;

    @Expose
    private Long timestampMillis;

    @Expose
    private String loggerName;

    @Expose
    private String level;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(Long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
