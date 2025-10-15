package com.overzealouspelican.frame;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.panel.ControlPanel;
import com.overzealouspelican.panel.MainContentPanel;
import com.overzealouspelican.panel.StatusPanel;
import com.overzealouspelican.panel.UrlPanel;

/**
 * Main application frame following Single Responsibility Principle.
 * Responsible for setting up the main window and orchestrating the layout.
 */
public class MainFrame extends JFrame {

    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;
    private static final int DIVIDER_LOCATION = 200;

    private final ControlPanel controlPanel;
    private final UrlPanel urlPanel;
    private final MainContentPanel mainContentPanel;
    private final StatusPanel statusPanel;
    private final JSplitPane splitPane;

    public MainFrame() {
        // Initialize all panels
        this.controlPanel = new ControlPanel();
        this.urlPanel = new UrlPanel();
        this.mainContentPanel = new MainContentPanel(urlPanel);
        this.statusPanel = new StatusPanel();
        this.splitPane = createSplitPane();

        initializeFrame();
        layoutComponents();
    }

    private void initializeFrame() {
        setTitle("YAPMC");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());
    }

    private void layoutComponents() {
        // Upper section (not resizable)
        add(controlPanel, BorderLayout.NORTH);

        // Center area with split pane
        add(splitPane, BorderLayout.CENTER);

        // Bottom status section
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JSplitPane createSplitPane() {
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setLeftComponent(urlPanel);
        pane.setRightComponent(mainContentPanel);
        pane.setDividerLocation(DIVIDER_LOCATION);
        return pane;
    }

    /**
     * Display the frame
     */
    public void display() {
        setVisible(true);
    }

    // Getters for accessing panels if needed for event handling
    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public UrlPanel getUrlPanel() {
        return urlPanel;
    }

    public MainContentPanel getMainContentPanel() {
        return mainContentPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }
}
