package com.overzealouspelican.panel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.service.ApiCallService;
import com.overzealouspelican.frame.ImportFrame;

/**
 * Modern IntelliJ-style saved calls panel with drag-and-drop grouping support.
 */
public class UrlPanel extends JPanel {

    private ApiCallService apiCallService;
    private ApplicationState appState;
    private JPanel listPanel;
    private CallConfigurationPanel configPanel;
    private Map<String, Boolean> groupExpandedState;

    public UrlPanel() {
        this.apiCallService = new ApiCallService();
        this.appState = ApplicationState.getInstance();
        this.groupExpandedState = new HashMap<>();
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

        // Organize API calls by group
        Map<String, List<String>> groups = new LinkedHashMap<>();
        List<String> ungrouped = new ArrayList<>();

        for (Map.Entry<String, ApiCall> entry : apiCalls.entrySet()) {
            String name = entry.getKey();
            String groupName = entry.getValue().getGroupName();

            if (groupName != null && !groupName.trim().isEmpty()) {
                groups.computeIfAbsent(groupName, k -> new ArrayList<>()).add(name);
            } else {
                ungrouped.add(name);
            }
        }

        // Add grouped items
        for (Map.Entry<String, List<String>> group : groups.entrySet()) {
            String groupName = group.getKey();
            List<String> members = group.getValue();
            listPanel.add(createGroupHeader(groupName, members));

            boolean expanded = groupExpandedState.getOrDefault(groupName, true);
            if (expanded) {
                for (String memberName : members) {
                    listPanel.add(createApiCallItem(memberName, groupName));
                }
            }
        }

        // Add ungrouped items
        for (String name : ungrouped) {
            listPanel.add(createApiCallItem(name, null));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createGroupHeader(String groupName, List<String> members) {
        JPanel headerPanel = new JPanel(new BorderLayout(6, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        boolean expanded = groupExpandedState.getOrDefault(groupName, true);

        // Expand/collapse icon
        JLabel iconLabel = new JLabel(expanded ? "▼" : "▶");
        iconLabel.setFont(iconLabel.getFont().deriveFont(10f));
        headerPanel.add(iconLabel, BorderLayout.WEST);

        // Group name
        JLabel nameLabel = new JLabel(groupName + " (" + members.size() + ")");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        headerPanel.add(nameLabel, BorderLayout.CENTER);

        // Click to expand/collapse
        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                groupExpandedState.put(groupName, !groupExpandedState.getOrDefault(groupName, true));
                loadApiCallsList();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                headerPanel.setBackground(UIManager.getColor("List.selectionBackground"));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                headerPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        // Setup drop target for the group header
        new DropTarget(headerPanel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    Transferable transferable = dtde.getTransferable();
                    String draggedName = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                    // Add to this group
                    addApiCallToGroup(draggedName, groupName);
                    dtde.dropComplete(true);
                    loadApiCallsList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dtde.dropComplete(false);
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                headerPanel.setBackground(new Color(100, 150, 255, 50));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                headerPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        return headerPanel;
    }

    private JPanel createApiCallItem(String name, String groupName) {
        JPanel itemPanel = new JPanel(new BorderLayout(6, 0));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        int leftPadding = groupName != null ? 24 : 8;
        itemPanel.setBorder(BorderFactory.createEmptyBorder(0, leftPadding, 0, 8));
        itemPanel.setBackground(UIManager.getColor("Panel.background"));
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
        deleteButton.addActionListener(e -> deleteApiCall(name));
        itemPanel.add(deleteButton, BorderLayout.EAST);

        // Click to load
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

        // Setup drag source
        DragSource dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(itemPanel, DnDConstants.ACTION_MOVE,
            new DragGestureListener() {
                @Override
                public void dragGestureRecognized(DragGestureEvent dge) {
                    Transferable transferable = new StringSelection(name);
                    dragSource.startDrag(dge, DragSource.DefaultMoveDrop, transferable, null);
                }
            }
        );

        // Setup drop target
        new DropTarget(itemPanel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    Transferable transferable = dtde.getTransferable();
                    String draggedName = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                    if (!draggedName.equals(name)) {
                        handleDrop(draggedName, name);
                    }
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dtde.dropComplete(false);
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                itemPanel.setBackground(new Color(100, 150, 255, 50));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                itemPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        return itemPanel;
    }

    private void handleDrop(String draggedName, String targetName) {
        try {
            Map<String, ApiCall> apiCalls = apiCallService.loadApiCalls();
            ApiCall draggedCall = apiCalls.get(draggedName);
            ApiCall targetCall = apiCalls.get(targetName);

            if (draggedCall == null || targetCall == null) {
                return;
            }

            String draggedGroup = draggedCall.getGroupName();
            String targetGroup = targetCall.getGroupName();

            // If dragged is a group member and target is a group member of different group, don't allow
            if (draggedGroup != null && targetGroup != null && !draggedGroup.equals(targetGroup)) {
                JOptionPane.showMessageDialog(this,
                    "Cannot nest groups. Please remove from current group first.",
                    "Invalid Operation",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // If target is in a group, add dragged to same group
            if (targetGroup != null && !targetGroup.trim().isEmpty()) {
                addApiCallToGroup(draggedName, targetGroup);
            } else {
                // Create new group or add to existing
                String groupName;
                if (draggedGroup != null && !draggedGroup.trim().isEmpty()) {
                    // Dragged is already in a group, add target to that group
                    addApiCallToGroup(targetName, draggedGroup);
                } else {
                    // Neither in a group, prompt for new group name
                    groupName = JOptionPane.showInputDialog(this,
                        "Enter a name for the new group:",
                        "Create Group",
                        JOptionPane.PLAIN_MESSAGE);

                    if (groupName != null && !groupName.trim().isEmpty()) {
                        addApiCallToGroup(draggedName, groupName);
                        addApiCallToGroup(targetName, groupName);
                    }
                }
            }

            loadApiCallsList();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to group API calls: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addApiCallToGroup(String apiCallName, String groupName) throws IOException {
        Map<String, ApiCall> apiCalls = apiCallService.loadApiCalls();
        ApiCall apiCall = apiCalls.get(apiCallName);

        if (apiCall != null) {
            apiCall.setGroupName(groupName);
            apiCallService.saveApiCall(apiCall);
            appState.setStatusSuccess("Added '" + apiCallName + "' to group '" + groupName + "'");
        }
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
