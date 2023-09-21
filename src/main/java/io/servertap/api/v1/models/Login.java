package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

public class Login {
    @Expose
    private String access_token = null;

    @Expose
    private String token_type = null;
}
