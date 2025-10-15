package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;

/**
 * Top content panel in the main content area.
 */
public class TopContentPanel extends JPanel {

    public TopContentPanel() {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Top Content Area"), BorderLayout.CENTER);
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
    }
}

