package io.servertap;

public class TestModel {

    private String message = "My Message";

    public TestModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
