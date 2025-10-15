package com.overzealouspelican.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a group of API calls.
 */
public class ApiCallGroup {
    private String name;
    private List<String> apiCallNames;
    private boolean expanded;

    public ApiCallGroup() {
        this.apiCallNames = new ArrayList<>();
        this.expanded = true;
    }

    public ApiCallGroup(String name) {
        this.name = name;
        this.apiCallNames = new ArrayList<>();
        this.expanded = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getApiCallNames() {
        return new ArrayList<>(apiCallNames);
    }

    public void setApiCallNames(List<String> apiCallNames) {
        this.apiCallNames = apiCallNames != null ? new ArrayList<>(apiCallNames) : new ArrayList<>();
    }

    public void addApiCall(String apiCallName) {
        if (!apiCallNames.contains(apiCallName)) {
            apiCallNames.add(apiCallName);
        }
    }

    public void removeApiCall(String apiCallName) {
        apiCallNames.remove(apiCallName);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isEmpty() {
        return apiCallNames.isEmpty();
    }

    @Override
    public String toString() {
        return "ApiCallGroup{name='" + name + "', apiCalls=" + apiCallNames.size() + "}";
    }
}

