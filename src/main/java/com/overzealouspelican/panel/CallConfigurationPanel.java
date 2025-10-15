package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import com.overzealouspelican.component.KeyValueInputGroup;
import com.overzealouspelican.component.LabeledTextField;
import com.overzealouspelican.component.UrlWithMethodInput;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.frame.CallOutputFrame;
import com.overzealouspelican.service.ApiCallService;

/**
 * Modern IntelliJ-style call configuration panel.
 */
public class CallConfigurationPanel extends JPanel {

    private JButton saveButton;
    private JButton callButton;
    private JButton clearButton;
    private LabeledTextField nameField;
    private UrlWithMethodInput urlInput;
    private KeyValueInputGroup headersGroup;
    private KeyValueInputGroup bodyGroup;
    private ApplicationState appState;
    private ApiCallService apiCallService;

    public CallConfigurationPanel() {
        this.appState = ApplicationState.getInstance();
        this.apiCallService = new ApiCallService();
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        // Add modern toolbar
        add(createToolbar(), BorderLayout.NORTH);

        // Main content area with padding
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(UIManager.getColor("Panel.background"));
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        add(contentWrapper, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UIManager.getColor("Panel.background"));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        // Left side - title
        JLabel titleLabel = new JLabel("API Request");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        toolbar.add(titleLabel, BorderLayout.WEST);

        // Right side - action buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonsPanel.setOpaque(false);

        clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear all form fields");
        clearButton.addActionListener(e -> handleClear());

        callButton = new JButton("Send");
        callButton.setToolTipText("Execute the API call");
        callButton.addActionListener(e -> handleCall());

        saveButton = new JButton("Save");
        saveButton.setToolTipText("Save this API call");
        saveButton.addActionListener(e -> handleSave());

        buttonsPanel.add(clearButton);
        buttonsPanel.add(callButton);
        buttonsPanel.add(saveButton);

        toolbar.add(buttonsPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        // Name input
        nameField = new LabeledTextField("Name", "Enter a name for this call");
        contentPanel.add(nameField);
        contentPanel.add(Box.createVerticalStrut(12));

        // URL input with HTTP method
        urlInput = new UrlWithMethodInput();
        contentPanel.add(urlInput);
        contentPanel.add(Box.createVerticalStrut(16));

        // Headers section
        headersGroup = new KeyValueInputGroup("Headers", "+ Add Header", "Remove this header");
        contentPanel.add(headersGroup);
        contentPanel.add(Box.createVerticalStrut(16));

        // Body section
        bodyGroup = new KeyValueInputGroup("Body", "+ Add Body Parameter", "Remove this body parameter");
        contentPanel.add(bodyGroup);
        contentPanel.add(Box.createVerticalStrut(16));

        return contentPanel;
    }

    private void handleCall() {
        // Update status to loading
        appState.setStatusLoading();

        // Gather call information
        String friendlyName = nameField.getText();
        String url = urlInput.getUrl();
        String httpMethod = urlInput.getHttpMethod();
        String environment = appState.getSelectedEnvironment();
        Map<String, String> environmentVariables = appState.getEnvironmentVariables();

        // Create ApiCall object
        ApiCall apiCall = new ApiCall(
            friendlyName,
            url,
            httpMethod,
            headersGroup.getKeyValuePairs(),
            bodyGroup.getKeyValuePairs()
        );

        // Execute the actual HTTP request in a background thread
        new Thread(() -> {
            ApiCallService.HttpCallResult result = apiCallService.executeApiCall(apiCall, environmentVariables);

            // Update UI on EDT
            SwingUtilities.invokeLater(() -> {
                // Format headers and body for display AFTER substitution
                StringBuilder headersDisplay = new StringBuilder();
                headersGroup.getKeyValuePairs().forEach((key, value) -> {
                    String resolvedKey = substituteVariables(key, environmentVariables);
                    String resolvedValue = substituteVariables(value, environmentVariables);
                    headersDisplay.append(resolvedKey).append(": ").append(resolvedValue).append("\n");
                });
                if (headersDisplay.length() == 0) {
                    headersDisplay.append("(No headers)");
                }

                StringBuilder bodyDisplay = new StringBuilder();
                bodyGroup.getKeyValuePairs().forEach((key, value) -> {
                    String resolvedKey = substituteVariables(key, environmentVariables);
                    String resolvedValue = substituteVariables(value, environmentVariables);
                    bodyDisplay.append(resolvedKey).append(": ").append(resolvedValue).append("\n");
                });
                if (bodyDisplay.length() == 0) {
                    bodyDisplay.append("(No body)");
                }

                // Show the output in the CallOutputFrame
                CallOutputFrame outputFrame = CallOutputFrame.getInstance();
                outputFrame.displayCallOutput(
                    environment,
                    friendlyName,
                    url,
                    httpMethod,
                    headersDisplay.toString(),
                    bodyDisplay.toString(),
                    result.formatResponse(),
                    environmentVariables
                );

                // Update status based on result
                if (result.isSuccess()) {
                    appState.setStatusSuccess("API call completed successfully");
                } else {
                    appState.setStatusError("API call failed");
                }
            });
        }).start();
    }

    /**
     * Substitute {{key}} placeholders with environment variable values
     */
    private String substituteVariables(String input, Map<String, String> environmentVariables) {
        if (input == null || environmentVariables == null) {
            return input;
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = environmentVariables.get(key);
            if (value != null) {
                matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(value));
            } else {
                // Keep the placeholder if no value found
                matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private void handleSave() {
        // Update status
        appState.setStatus("Saving configuration...", "🔵");

        String friendlyName = nameField.getText();
        String url = urlInput.getUrl();
        String httpMethod = urlInput.getHttpMethod();

        if (friendlyName == null || friendlyName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a name for this API call.",
                "Name Required",
                JOptionPane.WARNING_MESSAGE);
            appState.setStatusError("Name is required");
            return;
        }

        try {
            // Create and save the API call
            ApiCall apiCall = new ApiCall(
                friendlyName,
                url,
                httpMethod,
                headersGroup.getKeyValuePairs(),
                bodyGroup.getKeyValuePairs()
            );

            apiCallService.saveApiCall(apiCall);

            JOptionPane.showMessageDialog(this,
                "API call saved successfully to:\n" + apiCallService.getApiCallsFilePath(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            // Update status to success
            appState.setStatusSuccess("Configuration saved");

            // Notify that a new call was saved (fire property change)
            appState.firePropertyChange("apiCallSaved", null, friendlyName);
        } catch (Exception e) {

            JOptionPane.showMessageDialog(this,
                "Failed to save API call: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);

            appState.setStatusError("Failed to save configuration");
        }
    }

    private void handleClear() {
        // Clear all fields
        nameField.setText("");
        urlInput.setUrl("");
        urlInput.setHttpMethod("GET");
        headersGroup.clear();
        bodyGroup.clear();

        // Reset status
        appState.setStatus("Ready", "✅");
    }

    /**
     * Load an API call into the form
     */
    public void loadApiCall(ApiCall apiCall) {
        if (apiCall == null) return;

        nameField.setText(apiCall.getName());
        urlInput.setUrl(apiCall.getUrl());
        urlInput.setHttpMethod(apiCall.getHttpMethod());
        headersGroup.setKeyValuePairs(apiCall.getHeaders());
        bodyGroup.setKeyValuePairs(apiCall.getBody());
    }

    // Public API for accessing/setting data
    public String getFriendlyName() {
        return nameField.getText();
    }

    public void setFriendlyName(String name) {
        nameField.setText(name);
    }

    public String getUrl() {
        return urlInput.getUrl();
    }

    public void setUrl(String url) {
        urlInput.setUrl(url);
    }

    public String getHttpMethod() {
        return urlInput.getHttpMethod();
    }

    public void setHttpMethod(String httpMethod) {
        urlInput.setHttpMethod(httpMethod);
    }

    public KeyValueInputGroup getHeadersGroup() {
        return headersGroup;
    }

    public KeyValueInputGroup getBodyGroup() {
        return bodyGroup;
    }
}
