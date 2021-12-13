package io.servertap.api.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsoleLine {

    @JsonProperty("msg")
    private String message;

    @JsonProperty("ts")
    private Long timestampMillis;

    @JsonProperty("l")
    private String loggerName;

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
}
