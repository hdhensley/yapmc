package com.overzealouspelican.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.overzealouspelican.model.ApiCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing HAR (HTTP Archive) files into ApiCall objects.
 */
public class HarParser {

    /**
     * Parse a HAR file content and extract all HTTP requests as ApiCall objects
     */
    public static List<ApiCall> parseHar(String harContent) throws IllegalArgumentException {
        if (harContent == null || harContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HAR content cannot be empty");
        }

        List<ApiCall> apiCalls = new ArrayList<>();

        try {
            JsonObject harJson = JsonParser.parseString(harContent).getAsJsonObject();

            // Navigate to log.entries
            if (!harJson.has("log")) {
                throw new IllegalArgumentException("Invalid HAR format: missing 'log' object");
            }

            JsonObject log = harJson.getAsJsonObject("log");
            if (!log.has("entries")) {
                throw new IllegalArgumentException("Invalid HAR format: missing 'entries' array");
            }

            JsonArray entries = log.getAsJsonArray("entries");

            // Parse each entry
            for (JsonElement entryElement : entries) {
                try {
                    JsonObject entry = entryElement.getAsJsonObject();
                    ApiCall apiCall = parseEntry(entry);
                    if (apiCall != null) {
                        apiCalls.add(apiCall);
                    }
                } catch (Exception e) {
                    // Skip invalid entries
                    System.err.println("Failed to parse HAR entry: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse HAR file: " + e.getMessage(), e);
        }

        if (apiCalls.isEmpty()) {
            throw new IllegalArgumentException("No valid HTTP requests found in HAR file");
        }

        return apiCalls;
    }

    /**
     * Parse a single HAR entry into an ApiCall
     */
    private static ApiCall parseEntry(JsonObject entry) {
        if (!entry.has("request")) {
            return null;
        }

        JsonObject request = entry.getAsJsonObject("request");

        // Extract method and URL
        String method = request.has("method") ? request.get("method").getAsString() : "GET";
        String url = request.has("url") ? request.get("url").getAsString() : "";

        if (url.isEmpty()) {
            return null;
        }

        // Extract headers
        Map<String, String> headers = new HashMap<>();
        if (request.has("headers")) {
            JsonArray headersArray = request.getAsJsonArray("headers");
            for (JsonElement headerElement : headersArray) {
                JsonObject header = headerElement.getAsJsonObject();
                String name = header.has("name") ? header.get("name").getAsString() : "";
                String value = header.has("value") ? header.get("value").getAsString() : "";
                if (!name.isEmpty()) {
                    headers.put(name, value);
                }
            }
        }

        // Extract body/post data
        Map<String, String> body = new HashMap<>();
        if (request.has("postData")) {
            JsonObject postData = request.getAsJsonObject("postData");

            if (postData.has("text")) {
                String text = postData.get("text").getAsString();

                // Try to parse as JSON
                if (text.startsWith("{") && text.endsWith("}")) {
                    try {
                        JsonObject jsonBody = JsonParser.parseString(text).getAsJsonObject();
                        for (String key : jsonBody.keySet()) {
                            JsonElement value = jsonBody.get(key);
                            body.put(key, value.isJsonPrimitive() ? value.getAsString() : value.toString());
                        }
                    } catch (Exception e) {
                        // If not valid JSON, store as raw data
                        body.put("data", text);
                    }
                } else {
                    body.put("data", text);
                }
            } else if (postData.has("params")) {
                // Handle form data
                JsonArray params = postData.getAsJsonArray("params");
                for (JsonElement paramElement : params) {
                    JsonObject param = paramElement.getAsJsonObject();
                    String name = param.has("name") ? param.get("name").getAsString() : "";
                    String value = param.has("value") ? param.get("value").getAsString() : "";
                    if (!name.isEmpty()) {
                        body.put(name, value);
                    }
                }
            }
        }

        // Create ApiCall
        ApiCall apiCall = new ApiCall();
        apiCall.setUrl(url);
        apiCall.setHttpMethod(method);
        apiCall.setHeaders(headers);
        apiCall.setBody(body);

        // Generate a name based on the URL
        String name = CurlParser.generateName(url);
        apiCall.setName(name);

        return apiCall;
    }
}

