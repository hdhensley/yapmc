package com.overzealouspelican.frame;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Frame to display the output of API calls.
 * Uses singleton pattern to reuse the same window for multiple calls.
 * Automatically updates with new call results.
 */
public class CallOutputFrame extends JFrame {

    private static CallOutputFrame instance;
    private JTextArea outputTextArea;
    private Gson prettyGson;

    private CallOutputFrame() {
        this.prettyGson = new GsonBuilder().setPrettyPrinting().create();
        initializeFrame();
        addComponents();
    }

    /**
     * Get the singleton instance of CallOutputFrame
     */
    public static synchronized CallOutputFrame getInstance() {
        if (instance == null) {
            instance = new CallOutputFrame();
        }
        return instance;
    }

    private void initializeFrame() {
        setTitle("API Call Output");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // Hide instead of dispose to reuse
        setLayout(new BorderLayout());
    }

    private void addComponents() {
        // Title bar with status
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(UIManager.getColor("Panel.background"));
        titleBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Call Output");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleBar.add(titleLabel, BorderLayout.WEST);

        add(titleBar, BorderLayout.NORTH);

        // Output text area with scroll
        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        outputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        JButton closeButton = new JButton("Close");
        closeButton.setToolTipText("Close this window");
        closeButton.addActionListener(e -> setVisible(false));

        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Display the output of an API call
     */
    public void displayCallOutput(String environment, String name, String url, String method,
                                   String headers, String body, String response, Map<String, String> environmentVariables) {
        // Substitute environment variables in URL, headers, and body for display
        String resolvedUrl = substituteVariables(url, environmentVariables);
        String resolvedHeaders = substituteVariables(headers, environmentVariables);
        String resolvedBody = substituteVariables(body, environmentVariables);

        // Check if response contains JSON content-type to enable pretty printing
        boolean isJsonResponse = isJsonContentType(response);
        String formattedResponse = isJsonResponse ? prettyPrintJson(response) : response;

        StringBuilder output = new StringBuilder();
        output.append("═══════════════════════════════════════════════════════════════\n");
        output.append("API CALL OUTPUT\n");
        output.append("═══════════════════════════════════════════════════════════════\n\n");

        output.append("Environment: ").append(environment).append("\n");
        output.append("Name: ").append(name).append("\n");
        output.append("URL: ").append(resolvedUrl).append("\n");
        output.append("Method: ").append(method).append("\n\n");

        output.append("───────────────────────────────────────────────────────────────\n");
        output.append("ENVIRONMENT VARIABLES:\n");
        output.append("───────────────────────────────────────────────────────────────\n");
        if (environmentVariables != null && !environmentVariables.isEmpty()) {
            environmentVariables.forEach((key, value) ->
                output.append(key).append(": ").append(value).append("\n")
            );
        } else {
            output.append("(No environment variables)\n");
        }
        output.append("\n");

        output.append("───────────────────────────────────────────────────────────────\n");
        output.append("HEADERS:\n");
        output.append("───────────────────────────────────────────────────────────────\n");
        output.append(resolvedHeaders).append("\n\n");

        output.append("───────────────────────────────────────────────────────────────\n");
        output.append("BODY:\n");
        output.append("───────────────────────────────────────────────────────────────\n");
        output.append(resolvedBody).append("\n\n");

        output.append("───────────────────────────────────────────────────────────────\n");
        output.append("RESPONSE:\n");
        output.append("───────────────────────────────────────────────────────────────\n");
        output.append(formattedResponse).append("\n");

        output.append("═══════════════════════════════════════════════════════════════\n");

        outputTextArea.setText(output.toString());
        outputTextArea.setCaretPosition(0); // Scroll to top

        // Show the frame if hidden
        if (!isVisible()) {
            setLocationRelativeTo(null); // Center on screen
            setVisible(true);
        }

        // Bring to front
        toFront();
        requestFocus();
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
     * Check if the response indicates JSON content type
     */
    private boolean isJsonContentType(String response) {
        if (response == null) {
            return false;
        }

        // Check if response headers contain application/json
        String lowerResponse = response.toLowerCase();
        return lowerResponse.contains("content-type:") &&
               (lowerResponse.contains("application/json") || lowerResponse.contains("application/vnd.api+json"));
    }

    /**
     * Pretty print JSON response body
     */
    private String prettyPrintJson(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }

        try {
            // Extract the response body (after "Response Body:" section)
            String[] parts = response.split("Response Body:\n", 2);
            if (parts.length < 2) {
                return response;
            }

            String headerPart = parts[0];
            String bodyPart = parts[1].trim();

            // Check if body is empty or error message
            if (bodyPart.isEmpty() || bodyPart.startsWith("(Empty") || bodyPart.startsWith("Error:")) {
                return response;
            }

            // Try to parse and pretty print the JSON
            JsonElement jsonElement = JsonParser.parseString(bodyPart);
            String prettyJson = prettyGson.toJson(jsonElement);

            // Reconstruct the response with pretty-printed JSON
            return headerPart + "Response Body:\n" + prettyJson;

        } catch (Exception e) {
            // If parsing fails, return original response
            return response;
        }
    }

    /**
     * Display a simple text message
     */
    public void displayMessage(String message) {
        outputTextArea.setText(message);
        outputTextArea.setCaretPosition(0);

        if (!isVisible()) {
            setLocationRelativeTo(null);
            setVisible(true);
        }

        toFront();
        requestFocus();
    }

    /**
     * Clear the output
     */
    public void clearOutput() {
        outputTextArea.setText("");
    }

    /**
     * Show the frame
     */
    public void display() {
        if (!isVisible()) {
            setLocationRelativeTo(null);
        }
        setVisible(true);
        toFront();
        requestFocus();
    }
}
