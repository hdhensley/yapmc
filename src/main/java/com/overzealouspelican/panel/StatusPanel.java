package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.model.ApplicationState;

/**
 * Modern IntelliJ-style status bar.
 */
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
        setBackground(UIManager.getColor("Panel.background"));

        // Create a panel to hold both icon and text
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        // Add status icon
        iconLabel = new JLabel(appState.getStatusIcon());
        iconLabel.setFont(iconLabel.getFont().deriveFont(12f));
        leftPanel.add(iconLabel);

        // Add status text
        statusLabel = new JLabel("Status: " + appState.getStatusMessage());
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        leftPanel.add(statusLabel);

        add(leftPanel, BorderLayout.WEST);

        // Add top border
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIManager.getColor("Component.borderColor")));
        setPreferredSize(new Dimension(0, 24));
    }

    private void setupListeners() {
        appState.addPropertyChangeListener(ApplicationState.PROPERTY_STATUS_MESSAGE, evt -> {
            statusLabel.setText("Status: " + evt.getNewValue());
        });

        appState.addPropertyChangeListener(ApplicationState.PROPERTY_STATUS_ICON, evt -> {
            iconLabel.setText((String) evt.getNewValue());
        });
    }

    public void setStatus(String status, String emoji) {
        appState.setStatus(status, emoji);
    }

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