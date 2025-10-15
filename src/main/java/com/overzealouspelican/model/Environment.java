package com.overzealouspelican.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing an environment with its variables.
 */
public class Environment {
    private String name;
    private Map<String, String> variables;

    public Environment() {
        this.variables = new HashMap<>();
    }

    public Environment(String name, Map<String, String> variables) {
        this.name = name;
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getVariables() {
        return new HashMap<>(variables);
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }

    public void addVariable(String key, String value) {
        this.variables.put(key, value);
    }

    public String getVariable(String key) {
        return this.variables.get(key);
    }

    @Override
    public String toString() {
        return "Environment{name='" + name + "', variables=" + variables + "}";
    }
}

