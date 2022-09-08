package io.servertap.api.v1.models;

import java.util.List;

public class Advancement {

    private String name;

    private List<String> criteria;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<String> criteria) {
        this.criteria = criteria;
    }

}
