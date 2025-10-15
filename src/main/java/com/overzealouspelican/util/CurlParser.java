package com.overzealouspelican.util;

import com.overzealouspelican.model.ApiCall;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing cURL commands into ApiCall objects.
 */
public class CurlParser {

    /**
     * Parse a cURL command string into an ApiCall object
     */
    public static ApiCall parseCurl(String curlCommand) throws IllegalArgumentException {
        if (curlCommand == null || curlCommand.trim().isEmpty()) {
            throw new IllegalArgumentException("cURL command cannot be empty");
        }

        // Remove line continuations and extra whitespace
        String normalized = curlCommand
            .replaceAll("\\\\\\s*\\n\\s*", " ")
            .replaceAll("\\s+", " ")
            .trim();

        // Extract URL
        String url = extractUrl(normalized);
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Could not extract URL from cURL command");
        }

        // Extract HTTP method (default to GET)
        String method = extractMethod(normalized);

        // Extract headers
        Map<String, String> headers = extractHeaders(normalized);

        // Extract body data
        Map<String, String> body = extractBody(normalized);

        // Create ApiCall
        ApiCall apiCall = new ApiCall();
        apiCall.setUrl(url);
        apiCall.setHttpMethod(method);
        apiCall.setHeaders(headers);
        apiCall.setBody(body);

        return apiCall;
    }

    /**
     * Extract URL from cURL command
     */
    private static String extractUrl(String curl) {
        // Match curl 'url' or curl "url" or curl url
        Pattern pattern = Pattern.compile("curl\\s+['\"]([^'\"]+)['\"]|curl\\s+([^\\s-]+)");
        Matcher matcher = pattern.matcher(curl);

        if (matcher.find()) {
            String url = matcher.group(1);
            if (url == null) {
                url = matcher.group(2);
            }
            return url;
        }

        return null;
    }

    /**
     * Extract HTTP method from cURL command
     */
    private static String extractMethod(String curl) {
        // Look for -X METHOD or --request METHOD
        Pattern pattern = Pattern.compile("-X\\s+([A-Z]+)|--request\\s+([A-Z]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(curl);

        if (matcher.find()) {
            String method = matcher.group(1);
            if (method == null) {
                method = matcher.group(2);
            }
            return method.toUpperCase();
        }

        // Check for -d or --data (implies POST)
        if (curl.contains("-d ") || curl.contains("--data")) {
            return "POST";
        }

        // Default to GET
        return "GET";
    }

    /**
     * Extract headers from cURL command
     */
    private static Map<String, String> extractHeaders(String curl) {
        Map<String, String> headers = new HashMap<>();

        // Match -H 'header' or -H "header" or --header
        Pattern pattern = Pattern.compile("-H\\s+['\"]([^'\"]+)['\"]|--header\\s+['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(curl);

        while (matcher.find()) {
            String header = matcher.group(1);
            if (header == null) {
                header = matcher.group(2);
            }

            if (header != null) {
                // Split header into key and value
                int colonIndex = header.indexOf(':');
                if (colonIndex > 0) {
                    String key = header.substring(0, colonIndex).trim();
                    String value = header.substring(colonIndex + 1).trim();
                    headers.put(key, value);
                }
            }
        }

        return headers;
    }

    /**
     * Extract body data from cURL command
     */
    private static Map<String, String> extractBody(String curl) {
        Map<String, String> body = new HashMap<>();

        // Match -d 'data' or --data 'data'
        Pattern pattern = Pattern.compile("-d\\s+['\"]([^'\"]+)['\"]|--data\\s+['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(curl);

        if (matcher.find()) {
            String data = matcher.group(1);
            if (data == null) {
                data = matcher.group(2);
            }

            if (data != null) {
                // Try to parse as JSON or form data
                if (data.startsWith("{") && data.endsWith("}")) {
                    // Simple JSON parsing - extract key-value pairs
                    parseJsonToMap(data, body);
                } else {
                    // Store as raw data with a generic key
                    body.put("data", data);
                }
            }
        }

        return body;
    }

    /**
     * Simple JSON parsing to extract key-value pairs
     */
    private static void parseJsonToMap(String json, Map<String, String> map) {
        // Remove outer braces
        json = json.substring(1, json.length() - 1).trim();

        // Simple key-value extraction (not a full JSON parser)
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"|\"([^\"]+)\"\\s*:\\s*([^,}]+)");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            if (key == null) {
                key = matcher.group(3);
                value = matcher.group(4);
            }

            if (key != null && value != null) {
                map.put(key.trim(), value.trim());
            }
        }
    }

    /**
     * Generate a suggested name for the API call based on the URL
     */
    public static String generateName(String url) {
        if (url == null || url.isEmpty()) {
            return "Imported cURL";
        }

        try {
            // Extract the path from URL
            Pattern pattern = Pattern.compile("https?://[^/]+/(.+?)(?:\\?|$)");
            Matcher matcher = pattern.matcher(url);

            if (matcher.find()) {
                String path = matcher.group(1);
                // Take the last segment of the path
                String[] segments = path.split("/");
                if (segments.length > 0) {
                    String lastSegment = segments[segments.length - 1];
                    // Convert kebab-case or snake_case to Title Case
                    String nameWithSpaces = lastSegment.replaceAll("[-_]", " ");
                    // Capitalize first letter of each word
                    StringBuilder titleCase = new StringBuilder();
                    boolean capitalizeNext = true;
                    for (char c : nameWithSpaces.toCharArray()) {
                        if (Character.isWhitespace(c)) {
                            capitalizeNext = true;
                            titleCase.append(c);
                        } else if (capitalizeNext) {
                            titleCase.append(Character.toUpperCase(c));
                            capitalizeNext = false;
                        } else {
                            titleCase.append(c);
                        }
                    }
                    return titleCase.toString();
                }
            }

            // Fallback to domain name
            pattern = Pattern.compile("https?://([^/]+)");
            matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Ignore and return default
        }

        return "Imported cURL";
    }
}
