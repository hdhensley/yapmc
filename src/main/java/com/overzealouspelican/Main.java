package com.overzealouspelican;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.overzealouspelican.frame.MainFrame;

/**
 * Application entry point.
 * Follows Single Responsibility Principle - only responsible for initializing and starting the application.
 */
public class Main {
    public static void main(String[] args) {
        // Set up the look and feel
        FlatDarkLaf.setup();

        // Create and display the main frame
        MainFrame mainFrame = new MainFrame();
        mainFrame.display();
    }
}
