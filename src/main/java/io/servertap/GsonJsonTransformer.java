package io.servertap;

import com.google.gson.Gson;
import spark.ResponseTransformer;

public class GsonJsonTransformer implements ResponseTransformer {

    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }


}