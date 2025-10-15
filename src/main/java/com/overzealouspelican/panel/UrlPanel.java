package com.overzealouspelican.panel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.service.ApiCallService;
import com.overzealouspelican.frame.ImportFrame;

public class UrlPanel extends JPanel {

    private ApiCallService apiCallService;
    private ApplicationState appState;
    private JPanel listPanel;
    private CallConfigurationPanel configPanel;

    public UrlPanel() {
        this.apiCallService = new ApiCallService();
        this.appState = ApplicationState.getInstance();
        initializePanel();
        setupListeners();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        // Title panel with label and import button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titlePanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel titleLabel = new JLabel("Saved Calls");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Import button
        JButton importButton = new JButton("Import");
        importButton.setToolTipText("Import an API call from JSON file");
        importButton.setFont(importButton.getFont().deriveFont(10f));
        importButton.addActionListener(e -> openImportFrame());
        titlePanel.add(importButton, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // Create scrollable panel for API call items
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Use UIManager colors that adapt to FlatLaf themes
        setBackground(UIManager.getColor("Panel.background"));

        // Add a right border to indicate this panel is resizable
        setBorder(new MatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        setPreferredSize(new Dimension(200, 0));

        // Load saved API calls
        loadApiCallsList();
    }

    /**
     * Set the configuration panel that will be updated when an API call is selected
     */
    public void setConfigurationPanel(CallConfigurationPanel configPanel) {
        this.configPanel = configPanel;
    }

    /**
     * Setup listeners for when new API calls are saved
     */
    private void setupListeners() {
        appState.addPropertyChangeListener("apiCallSaved", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                loadApiCallsList();
            }
        });
    }

    /**
     * Load all saved API calls into the list
     */
    private void loadApiCallsList() {
        listPanel.removeAll();
        Map<String, ApiCall> apiCalls = apiCallService.loadApiCalls();

        for (Map.Entry<String, ApiCall> entry : apiCalls.entrySet()) {
            String name = entry.getKey();
            listPanel.add(createApiCallItem(name));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Create a panel for a single API call item with name and delete button
     */
    private JPanel createApiCallItem(String name) {
        JPanel itemPanel = new JPanel(new BorderLayout(5, 0));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        itemPanel.setBackground(UIManager.getColor("Panel.background"));

        // Make the panel clickable
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loadApiCall(name);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("List.selectionBackground"));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        // API call name label
        JLabel nameLabel = new JLabel(name);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        itemPanel.add(nameLabel, BorderLayout.CENTER);

        // Delete button
        JButton deleteButton = new JButton("✕");
        deleteButton.setPreferredSize(new Dimension(30, 25));
        deleteButton.setToolTipText("Delete this saved call");
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> {
            deleteApiCall(name);
        });
        itemPanel.add(deleteButton, BorderLayout.EAST);

        return itemPanel;
    }

    /**
     * Load the selected API call into the configuration panel
     */
    private void loadApiCall(String name) {
        if (name == null || configPanel == null) return;

        ApiCall apiCall = apiCallService.loadApiCall(name);
        if (apiCall != null) {
            configPanel.loadApiCall(apiCall);
            appState.setStatus("Loaded: " + name, "📋");
        }
    }

    /**
     * Delete an API call after confirmation
     */
    private void deleteApiCall(String name) {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete '" + name + "'?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            try {
                apiCallService.deleteApiCall(name);
                appState.setStatusSuccess("Deleted: " + name);
                loadApiCallsList();
            } catch (Exception e) {
                System.err.println("Failed to delete API call: " + e.getMessage());
                e.printStackTrace();

                JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete API call: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );

                appState.setStatusError("Failed to delete call");
            }
        }
    }

    /**
     * Open the import frame to import API calls from JSON files
     */
    private void openImportFrame() {
        ImportFrame importFrame = new ImportFrame();
        importFrame.display();

        // Refresh the list after the import frame is closed
        importFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadApiCallsList();
            }
        });
    }

    /**
     * Refresh the list of API calls
     */
    public void refresh() {
        loadApiCallsList();
    }
}
