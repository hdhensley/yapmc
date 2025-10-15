package com.overzealouspelican;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.overzealouspelican.frame.MainFrame;
import com.overzealouspelican.panel.SettingsEditorPanel;

/**
 * Application entry point.
 * Follows Single Responsibility Principle - only responsible for initializing and starting the application.
 */
public class Main {
    public static void main(String[] args) {
        // Load and apply saved theme before creating any UI
        SettingsEditorPanel.loadAndApplyTheme();

        // Set FlatLaf Look and Feel as fallback if no theme is saved
        try {
            if (UIManager.getLookAndFeel().getClass().getName().contains("Metal") ||
                UIManager.getLookAndFeel().getClass().getName().contains("Nimbus")) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // Create and display the main frame
        MainFrame mainFrame = new MainFrame();
        mainFrame.display();
    }
}
