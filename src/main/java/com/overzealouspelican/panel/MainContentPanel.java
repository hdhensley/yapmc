package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;

public class MainContentPanel extends JPanel {

    private final CallConfigurationPanel callConfigurationPanel;

    public MainContentPanel(UrlPanel urlPanel) {
        this.callConfigurationPanel = new CallConfigurationPanel();

        // Connect the URL panel with the configuration panel
        if (urlPanel != null) {
            urlPanel.setConfigurationPanel(callConfigurationPanel);
        }

        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        add(callConfigurationPanel, BorderLayout.CENTER);

        // Use UIManager colors that adapt to FlatLaf themes
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Component.borderColor")));
    }

    // Getter for accessing the configuration panel
    public CallConfigurationPanel getCallConfigurationPanel() {
        return callConfigurationPanel;
    }
}
