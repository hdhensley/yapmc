package com.overzealouspelican.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.model.Environment;
import com.overzealouspelican.service.EnvironmentService;
import com.overzealouspelican.frame.SettingsFrame;

/**
 * IntelliJ-style sidebar panel with collapsible sections for Environments and Settings.
 */
public class SidebarPanel extends JPanel {

    private final ApplicationState appState;
    private final EnvironmentService environmentService;
    private JComboBox<String> environmentDropdown;
    private EnvironmentEditorPanel environmentEditor;
    private SettingsEditorPanel settingsEditor;

    public SidebarPanel() {
        this.appState = ApplicationState.getInstance();
        this.environmentService = new EnvironmentService();
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        // Create tabbed pane for different sections
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(11f));

        // Environment tab
        environmentEditor = new EnvironmentEditorPanel();
        tabbedPane.addTab("Environments", environmentEditor);

        // Settings tab
        settingsEditor = new SettingsEditorPanel();
        tabbedPane.addTab("Settings", settingsEditor);

        add(tabbedPane, BorderLayout.CENTER);

        // Add border (bottom only since we're stacking vertically)
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
            UIManager.getColor("Component.borderColor")));
    }

    public void refresh() {
        environmentEditor.refresh();
    }
}
