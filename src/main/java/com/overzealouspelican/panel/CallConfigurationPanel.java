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
 * Call configuration panel in the main content area.
 * Refactored to follow SOLID principles with reusable components.
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

        // Add title bar
        add(createTitleBar(), BorderLayout.NORTH);

        // Main content area
        add(createContentPanel(), BorderLayout.CENTER);

        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(UIManager.getColor("Panel.background"));
        titleBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Left side - title
        JLabel titleLabel = new JLabel("URL Information");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleBar.add(titleLabel, BorderLayout.WEST);

        // Right side - buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        // Clear button
        clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear all form fields");
        clearButton.addActionListener(e -> handleClear());
        buttonsPanel.add(clearButton);

        // Call button
        callButton = new JButton("Call");
        callButton.setToolTipText("Execute the API call");
        callButton.addActionListener(e -> handleCall());
        buttonsPanel.add(callButton);

        // Save button
        saveButton = new JButton("Save");
        saveButton.setToolTipText("Save the call configuration");
        saveButton.addActionListener(e -> handleSave());
        buttonsPanel.add(saveButton);

        titleBar.add(buttonsPanel, BorderLayout.EAST);

        return titleBar;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        // Name input
        nameField = new LabeledTextField("Name", "Enter a name for this call");
        contentPanel.add(nameField);
        contentPanel.add(Box.createVerticalStrut(5));

        // URL input with HTTP method
        urlInput = new UrlWithMethodInput();
        contentPanel.add(urlInput);
        contentPanel.add(Box.createVerticalStrut(5));

        // Headers section
        headersGroup = new KeyValueInputGroup("Headers", "+ Add Header", "Remove this header");
        contentPanel.add(headersGroup);
        contentPanel.add(Box.createVerticalStrut(5));

        // Body section
        bodyGroup = new KeyValueInputGroup("Body", "+ Add Body Parameter", "Remove this body parameter");
        contentPanel.add(bodyGroup);

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

        // Format headers and body for display
        StringBuilder headersDisplay = new StringBuilder();
        headersGroup.getKeyValuePairs().forEach((key, value) ->
            headersDisplay.append(key).append(": ").append(value).append("\n")
        );
        if (headersDisplay.length() == 0) {
            headersDisplay.append("(No headers)");
        }

        StringBuilder bodyDisplay = new StringBuilder();
        bodyGroup.getKeyValuePairs().forEach((key, value) ->
            bodyDisplay.append(key).append(": ").append(value).append("\n")
        );
        if (bodyDisplay.length() == 0) {
            bodyDisplay.append("(No body)");
        }

        System.out.println("Call button clicked in CallConfigurationPanel");
        System.out.println("Executing API call:");
        System.out.println("  Environment: " + environment);
        System.out.println("  Name: " + friendlyName);
        System.out.println("  URL: " + url);
        System.out.println("  HTTP Method: " + httpMethod);
        System.out.println("  Headers: " + headersGroup.getKeyValuePairs());
        System.out.println("  Body: " + bodyGroup.getKeyValuePairs());

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

            System.out.println("Save button clicked in CallConfigurationPanel");
            System.out.println("Friendly Name: " + friendlyName);
            System.out.println("URL: " + url);
            System.out.println("HTTP Method: " + httpMethod);
            System.out.println("Headers: " + headersGroup.getKeyValuePairs());
            System.out.println("Body: " + bodyGroup.getKeyValuePairs());
            System.out.println("Saved to: " + apiCallService.getApiCallsFilePath());

            JOptionPane.showMessageDialog(this,
                "API call saved successfully to:\n" + apiCallService.getApiCallsFilePath(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            // Update status to success
            appState.setStatusSuccess("Configuration saved");

            // Notify that a new call was saved (fire property change)
            appState.firePropertyChange("apiCallSaved", null, friendlyName);
        } catch (Exception e) {
            System.err.println("Failed to save API call: " + e.getMessage());
            e.printStackTrace();

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
