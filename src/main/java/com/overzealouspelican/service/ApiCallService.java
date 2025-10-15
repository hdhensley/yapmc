package com.overzealouspelican.service;

import com.overzealouspelican.model.ApiCall;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for persisting API calls to JSON files on the local filesystem.
 * Follows Single Responsibility Principle - handles only API call persistence.
 */
public class ApiCallService {

    private static final String APP_DIR_NAME = ".yapmc";
    private static final String API_CALLS_FILE = "api-calls.json";
    private final Gson gson;
    private final Path dataDirectory;
    private final Path apiCallsFile;
    private final HttpClient httpClient;

    public ApiCallService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataDirectory = getDataDirectory();
        this.apiCallsFile = dataDirectory.resolve(API_CALLS_FILE);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        ensureDataDirectoryExists();
    }

    /**
     * Get the application data directory based on the OS
     */
    private Path getDataDirectory() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "YAPMC");
            }
            return Paths.get(userHome, "AppData", "Roaming", "YAPMC");
        } else if (os.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", "YAPMC");
        } else {
            return Paths.get(userHome, APP_DIR_NAME);
        }
    }

    /**
     * Ensure the data directory exists
     */
    private void ensureDataDirectoryExists() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
                System.out.println("Created data directory: " + dataDirectory);
            }
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all API calls from the JSON file
     */
    public Map<String, ApiCall> loadApiCalls() {
        if (!Files.exists(apiCallsFile)) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(apiCallsFile.toFile())) {
            Type type = new TypeToken<Map<String, ApiCall>>(){}.getType();
            Map<String, ApiCall> apiCalls = gson.fromJson(reader, type);
            return apiCalls != null ? apiCalls : new HashMap<>();
        } catch (IOException e) {
            System.err.println("Failed to load API calls: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Save all API calls to the JSON file
     */
    public void saveApiCalls(Map<String, ApiCall> apiCalls) throws IOException {
        try (FileWriter writer = new FileWriter(apiCallsFile.toFile())) {
            gson.toJson(apiCalls, writer);
            System.out.println("Saved API calls to: " + apiCallsFile);
        }
    }

    /**
     * Save a single API call
     */
    public void saveApiCall(ApiCall apiCall) throws IOException {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        apiCalls.put(apiCall.getName(), apiCall);
        saveApiCalls(apiCalls);
    }

    /**
     * Load a specific API call by name
     */
    public ApiCall loadApiCall(String name) {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        return apiCalls.get(name);
    }

    /**
     * Delete an API call
     */
    public void deleteApiCall(String name) throws IOException {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        apiCalls.remove(name);
        saveApiCalls(apiCalls);
    }

    /**
     * Check if an API call exists
     */
    public boolean apiCallExists(String name) {
        Map<String, ApiCall> apiCalls = loadApiCalls();
        return apiCalls.containsKey(name);
    }

    /**
     * Get the path to the API calls file
     */
    public String getApiCallsFilePath() {
        return apiCallsFile.toString();
    }

    /**
     * Execute an API call with environment variable substitution
     */
    public HttpCallResult executeApiCall(ApiCall apiCall, Map<String, String> environmentVariables) {
        try {
            // Log environment variables for debugging
            System.out.println("Environment variables available: " + environmentVariables);

            // Substitute environment variables in URL
            String resolvedUrl = substituteVariables(apiCall.getUrl(), environmentVariables);
            System.out.println("Original URL: " + apiCall.getUrl());
            System.out.println("Resolved URL: " + resolvedUrl);

            // Check if URL still contains unresolved variables
            if (resolvedUrl.contains("{{") && resolvedUrl.contains("}}")) {
                Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
                Matcher matcher = pattern.matcher(resolvedUrl);
                StringBuilder missingVars = new StringBuilder();
                while (matcher.find()) {
                    if (missingVars.length() > 0) missingVars.append(", ");
                    missingVars.append(matcher.group(1));
                }
                throw new IllegalArgumentException(
                    "URL contains unresolved environment variables: " + missingVars.toString() +
                    "\nAvailable variables: " + environmentVariables.keySet()
                );
            }

            // Substitute environment variables in headers
            Map<String, String> resolvedHeaders = new HashMap<>();
            for (Map.Entry<String, String> entry : apiCall.getHeaders().entrySet()) {
                String key = substituteVariables(entry.getKey(), environmentVariables);
                String value = substituteVariables(entry.getValue(), environmentVariables);
                resolvedHeaders.put(key, value);
            }

            // Substitute environment variables in body
            Map<String, String> resolvedBody = new HashMap<>();
            for (Map.Entry<String, String> entry : apiCall.getBody().entrySet()) {
                String key = substituteVariables(entry.getKey(), environmentVariables);
                String value = substituteVariables(entry.getValue(), environmentVariables);
                resolvedBody.put(key, value);
            }

            // Build the request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(resolvedUrl))
                .timeout(Duration.ofSeconds(30));

            // Add headers
            for (Map.Entry<String, String> header : resolvedHeaders.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            // Set the HTTP method and body
            String method = apiCall.getHttpMethod().toUpperCase();
            String bodyContent = buildBodyContent(resolvedBody);

            switch (method) {
                case "GET":
                    requestBuilder.GET();
                    break;
                case "POST":
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyContent));
                    if (!resolvedHeaders.containsKey("Content-Type")) {
                        requestBuilder.header("Content-Type", "application/json");
                    }
                    break;
                case "PUT":
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(bodyContent));
                    if (!resolvedHeaders.containsKey("Content-Type")) {
                        requestBuilder.header("Content-Type", "application/json");
                    }
                    break;
                case "DELETE":
                    requestBuilder.DELETE();
                    break;
                case "PATCH":
                    requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(bodyContent));
                    if (!resolvedHeaders.containsKey("Content-Type")) {
                        requestBuilder.header("Content-Type", "application/json");
                    }
                    break;
                case "HEAD":
                    requestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                    break;
                case "OPTIONS":
                    requestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
                    break;
                default:
                    requestBuilder.GET();
            }

            HttpRequest request = requestBuilder.build();

            // Execute the request
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Build result
            return new HttpCallResult(
                response.statusCode(),
                response.body(),
                response.headers().map(),
                duration,
                null
            );

        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace
            return new HttpCallResult(
                0,
                "Error: " + e.getMessage(),
                new HashMap<>(),
                0,
                e
            );
        }
    }

    /**
     * Substitute {{key}} placeholders with environment variable values
     */
    private String substituteVariables(String input, Map<String, String> environmentVariables) {
        if (input == null || environmentVariables == null) {
            return input;
        }

        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = environmentVariables.get(key);
            if (value != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(value));
            } else {
                // Keep the placeholder if no value found
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Build JSON body content from key-value pairs
     */
    private String buildBodyContent(Map<String, String> body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        return gson.toJson(body);
    }

    /**
     * Result object for HTTP calls
     */
    public static class HttpCallResult {
        private final int statusCode;
        private final String body;
        private final Map<String, java.util.List<String>> headers;
        private final long duration;
        private final Exception error;

        public HttpCallResult(int statusCode, String body, Map<String, java.util.List<String>> headers,
                            long duration, Exception error) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
            this.duration = duration;
            this.error = error;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public Map<String, java.util.List<String>> getHeaders() {
            return headers;
        }

        public long getDuration() {
            return duration;
        }

        public Exception getError() {
            return error;
        }

        public boolean isSuccess() {
            return error == null && statusCode >= 200 && statusCode < 300;
        }

        public String formatResponse() {
            if (error != null) {
                return "Error: " + error.getMessage() + "\n\nStack trace:\n" + getStackTraceString(error);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Status: ").append(statusCode).append("\n");
            sb.append("Duration: ").append(duration).append(" ms\n\n");

            sb.append("Response Headers:\n");
            if (headers != null && !headers.isEmpty()) {
                headers.forEach((key, values) -> {
                    sb.append(key).append(": ").append(String.join(", ", values)).append("\n");
                });
            } else {
                sb.append("(No headers)\n");
            }

            sb.append("\nResponse Body:\n");
            sb.append(body != null && !body.isEmpty() ? body : "(Empty response)");

            return sb.toString();
        }

        private String getStackTraceString(Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }
}
