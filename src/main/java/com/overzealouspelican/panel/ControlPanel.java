package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.frame.EnvironmentFrame;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.model.Environment;
import com.overzealouspelican.service.EnvironmentService;

public class ControlPanel extends JPanel {

    private JButton manageButton;
    private JComboBox<String> environmentDropdown;
    private ApplicationState appState;
    private EnvironmentService environmentService;

    public ControlPanel() {
        this.appState = ApplicationState.getInstance();
        this.environmentService = new EnvironmentService();
        initializePanel();
        // Load initial environment variables
        loadEnvironmentVariables((String) environmentDropdown.getSelectedItem());
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Right side - dropdown and buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        // Environment dropdown
        String[] environments = {"Development", "Staging", "Production", "Testing", "Local"};
        environmentDropdown = new JComboBox<>(environments);
        environmentDropdown.setPreferredSize(new Dimension(150, 25));
        environmentDropdown.setToolTipText("Select environment");
        environmentDropdown.setSelectedItem(appState.getSelectedEnvironment());

        // Listen for changes and update application state
        environmentDropdown.addActionListener(e -> {
            String selected = (String) environmentDropdown.getSelectedItem();
            appState.setSelectedEnvironment(selected);
            loadEnvironmentVariables(selected);
        });

        rightPanel.add(environmentDropdown);

        // Settings button
        JButton settingsButton = new JButton("Settings");
        settingsButton.setToolTipText("Application settings");
        settingsButton.addActionListener(e -> openSettingsFrame());
        rightPanel.add(settingsButton);

        // Manage button
        manageButton = new JButton("Manage");
        manageButton.setToolTipText("Manage environments");
        manageButton.addActionListener(e -> openEnvironmentFrame());
        rightPanel.add(manageButton);

        add(rightPanel, BorderLayout.EAST);

        // Use UIManager colors that adapt to FlatLaf themes
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(800, 40)); // Reduced from 60 to 40
    }

    private void openEnvironmentFrame() {
        EnvironmentFrame environmentFrame = new EnvironmentFrame();
        environmentFrame.display();
    }

    private void openSettingsFrame() {
        // TODO: Create and open settings frame
        System.out.println("Settings button clicked - Settings frame not yet implemented");
        appState.setStatus("Settings", "⚙️");
    }

    private void loadEnvironmentVariables(String environmentName) {
        if (environmentName == null || environmentName.isEmpty()) {
            return;
        }

        Environment environment = environmentService.loadEnvironment(environmentName);
        if (environment != null && environment.getVariables() != null) {
            appState.setEnvironmentVariables(environment.getVariables());
        } else {
            // Clear environment variables if environment doesn't exist or has no variables
            appState.setEnvironmentVariables(new java.util.HashMap<>());
        }
    }

    public String getSelectedEnvironment() {
        return appState.getSelectedEnvironment();
    }

    public void setSelectedEnvironment(String environment) {
        environmentDropdown.setSelectedItem(environment);
        appState.setSelectedEnvironment(environment);
        loadEnvironmentVariables(environment);
    }

    public void addEnvironment(String environment) {
        environmentDropdown.addItem(environment);
    }
}