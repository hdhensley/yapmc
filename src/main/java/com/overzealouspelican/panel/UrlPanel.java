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

/**
 * Modern IntelliJ-style saved calls panel.
 */
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
        setBackground(UIManager.getColor("Panel.background"));

        // Modern toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        toolbar.setBackground(UIManager.getColor("Panel.background"));

        JLabel titleLabel = new JLabel("Saved Calls");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        toolbar.add(titleLabel, BorderLayout.WEST);

        // Import button
        JButton importButton = new JButton("Import");
        importButton.setToolTipText("Import an API call from JSON file");
        importButton.setFont(importButton.getFont().deriveFont(11f));
        importButton.addActionListener(e -> openImportFrame());
        toolbar.add(importButton, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);

        // Create scrollable panel for API call items
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UIManager.getColor("Panel.background"));
        listPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Remove the right border since we're stacking vertically now
        setPreferredSize(new Dimension(320, 0));

        loadApiCallsList();
    }

    public void setConfigurationPanel(CallConfigurationPanel configPanel) {
        this.configPanel = configPanel;
    }

    private void setupListeners() {
        appState.addPropertyChangeListener("apiCallSaved", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                loadApiCallsList();
            }
        });
    }

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

    private JPanel createApiCallItem(String name) {
        JPanel itemPanel = new JPanel(new BorderLayout(6, 0));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        itemPanel.setBackground(UIManager.getColor("Panel.background"));

        // Make the panel clickable
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color originalBg = UIManager.getColor("Panel.background");

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
                itemPanel.setBackground(originalBg);
            }
        });

        // API call name label
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 12f));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        itemPanel.add(nameLabel, BorderLayout.CENTER);

        // Delete button
        JButton deleteButton = new JButton("×");
        deleteButton.setPreferredSize(new Dimension(28, 24));
        deleteButton.setToolTipText("Delete this saved call");
        deleteButton.setFocusPainted(false);
        deleteButton.setFont(deleteButton.getFont().deriveFont(16f));
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.addActionListener(e -> {
            // Stop event propagation to prevent triggering the panel click
            deleteApiCall(name);
        });
        itemPanel.add(deleteButton, BorderLayout.EAST);

        return itemPanel;
    }

    private void loadApiCall(String name) {
        if (name == null || configPanel == null) return;

        ApiCall apiCall = apiCallService.loadApiCall(name);
        if (apiCall != null) {
            configPanel.loadApiCall(apiCall);
            appState.setStatus("Loaded: " + name, "📋");
        }
    }

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

    private void openImportFrame() {
        ImportFrame importFrame = new ImportFrame();
        importFrame.display();

        importFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadApiCallsList();
            }
        });
    }

    public void refresh() {
        loadApiCallsList();
    }
}
