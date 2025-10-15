package com.overzealouspelican.frame;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.panel.*;
import com.overzealouspelican.model.ApplicationState;

/**
 * Main application frame with IntelliJ-style modern UI.
 * Features a stacked sidebar above saved calls, modern toolbar, and improved layout.
 */
public class MainFrame extends JFrame {

    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int LEFT_PANEL_WIDTH = 420;
    private static final int SIDEBAR_DIVIDER = 420;

    private final ToolbarPanel toolbarPanel;
    private final SidebarPanel sidebarPanel;
    private final UrlPanel urlPanel;
    private final MainContentPanel mainContentPanel;
    private final StatusPanel statusPanel;
    private final JSplitPane mainSplitPane;
    private final JSplitPane leftSplitPane;
    private boolean sidebarVisible = true;
    private ApplicationState appState;

    public MainFrame() {
        this.appState = ApplicationState.getInstance();

        // Initialize panels
        this.sidebarPanel = new SidebarPanel();
        this.urlPanel = new UrlPanel();
        this.mainContentPanel = new MainContentPanel(urlPanel);
        this.statusPanel = new StatusPanel();
        this.toolbarPanel = new ToolbarPanel(this::toggleSidebar);

        this.leftSplitPane = createLeftSplitPane();
        this.mainSplitPane = createMainSplitPane();

        initializeFrame();
        layoutComponents();
        setupListeners();
    }

    private void initializeFrame() {
        setTitle("YAPMC - Yet Another Postman Clone");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void layoutComponents() {
        // Toolbar at top
        add(toolbarPanel, BorderLayout.NORTH);

        // Main split pane in center
        add(mainSplitPane, BorderLayout.CENTER);

        // Status bar at bottom
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JSplitPane createLeftSplitPane() {
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pane.setTopComponent(sidebarPanel);
        pane.setBottomComponent(urlPanel);
        pane.setDividerLocation(SIDEBAR_DIVIDER);
        pane.setOneTouchExpandable(false);
        pane.setBorder(null);
        pane.setResizeWeight(0.5); // Give equal weight for resizing
        return pane;
    }

    private JSplitPane createMainSplitPane() {
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setLeftComponent(leftSplitPane);
        pane.setRightComponent(mainContentPanel);
        pane.setDividerLocation(LEFT_PANEL_WIDTH);
        pane.setOneTouchExpandable(false);
        pane.setBorder(null);
        return pane;
    }

    private void setupListeners() {
        // Listen for environment changes
        appState.addPropertyChangeListener("selectedEnvironment", evt -> {
            String environment = (String) evt.getNewValue();
            toolbarPanel.setEnvironmentLabel(environment);
        });

        // Set initial environment
        toolbarPanel.setEnvironmentLabel(appState.getSelectedEnvironment());
    }

    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        if (sidebarVisible) {
            leftSplitPane.setDividerLocation(SIDEBAR_DIVIDER);
            leftSplitPane.setDividerSize(5);
        } else {
            leftSplitPane.setDividerLocation(0);
            leftSplitPane.setDividerSize(0);
        }
    }

    /**
     * Display the frame
     */
    public void display() {
        setVisible(true);
    }

    // Getters for accessing panels if needed
    public UrlPanel getUrlPanel() {
        return urlPanel;
    }

    public MainContentPanel getMainContentPanel() {
        return mainContentPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public SidebarPanel getSidebarPanel() {
        return sidebarPanel;
    }
}
