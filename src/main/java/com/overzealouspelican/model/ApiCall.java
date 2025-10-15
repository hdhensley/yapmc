package com.overzealouspelican.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a saved API call configuration.
 */
public class ApiCall {
    private String name;
    private String url;
    private String httpMethod;
    private Map<String, String> headers;
    private Map<String, String> body;
    private String groupName; // Group this API call belongs to

    public ApiCall() {
        this.headers = new HashMap<>();
        this.body = new HashMap<>();
    }

    public ApiCall(String name, String url, String httpMethod, Map<String, String> headers, Map<String, String> body) {
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.body = body != null ? new HashMap<>(body) : new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
    }

    public Map<String, String> getBody() {
        return new HashMap<>(body);
    }

    public void setBody(Map<String, String> body) {
        this.body = body != null ? new HashMap<>(body) : new HashMap<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "ApiCall{name='" + name + "', url='" + url + "', method='" + httpMethod + "'}";
    }
}
