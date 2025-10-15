package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.model.ApplicationState;

public class StatusPanel extends JPanel {

    private JLabel statusLabel;
    private JLabel iconLabel;
    private ApplicationState appState;

    public StatusPanel() {
        this.appState = ApplicationState.getInstance();
        initializePanel();
        setupListeners();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Create a panel to hold both icon and text
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        // Add status icon using emoji - initialize from app state
        iconLabel = new JLabel(appState.getStatusIcon());
        leftPanel.add(iconLabel);

        // Add status text - initialize from app state
        statusLabel = new JLabel("Status: " + appState.getStatusMessage());
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Set monospaced font
        leftPanel.add(statusLabel);

        add(leftPanel, BorderLayout.WEST);

        // Use UIManager colors that adapt to FlatLaf themes
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(800, 25));
    }

    /**
     * Set up listeners to automatically update when ApplicationState changes
     */
    private void setupListeners() {
        // Listen for status message changes
        appState.addPropertyChangeListener(ApplicationState.PROPERTY_STATUS_MESSAGE, evt -> {
            statusLabel.setText("Status: " + evt.getNewValue());
        });

        // Listen for status icon changes
        appState.addPropertyChangeListener(ApplicationState.PROPERTY_STATUS_ICON, evt -> {
            iconLabel.setText((String) evt.getNewValue());
        });
    }

    /**
     * Update the status with text and emoji (backwards compatibility)
     * Now delegates to ApplicationState
     */
    public void setStatus(String status, String emoji) {
        appState.setStatus(status, emoji);
    }

    /**
     * Convenience methods for common status updates (backwards compatibility)
     * Now delegates to ApplicationState
     */
    public void setStatusReady() {
        appState.setStatusReady();
    }

    public void setStatusWorking() {
        appState.setStatusLoading();
    }

    public void setStatusError() {
        appState.setStatusError("");
    }
}