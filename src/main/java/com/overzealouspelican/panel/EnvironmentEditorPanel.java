package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.model.Environment;
import com.overzealouspelican.service.EnvironmentService;

/**
 * IntelliJ-style environment editor embedded in the sidebar.
 */
public class EnvironmentEditorPanel extends JPanel {

    private static final int INITIAL_KEY_VALUE_ROWS = 3;

    private JComboBox<String> environmentDropdown;
    private List<JTextField> keyFields;
    private List<JTextField> valueFields;
    private List<JButton> removeButtons;
    private JPanel keyValueRowsContainer;
    private ApplicationState appState;
    private EnvironmentService environmentService;
    private JButton saveButton;
    private JButton newEnvButton;

    public EnvironmentEditorPanel() {
        keyFields = new ArrayList<>();
        valueFields = new ArrayList<>();
        removeButtons = new ArrayList<>();
        appState = ApplicationState.getInstance();
        environmentService = new EnvironmentService();
        initializePanel();
        loadEnvironmentsFromDisk();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIManager.getColor("Panel.background"));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Dropdown section
        mainPanel.add(createDropdownPanel());
        mainPanel.add(Box.createVerticalStrut(12));

        // Key-value pairs section
        mainPanel.add(createKeyValuePanel());
        mainPanel.add(Box.createVerticalStrut(12));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDropdownPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.setBackground(UIManager.getColor("Panel.background"));

        environmentDropdown = new JComboBox<>();
        environmentDropdown.addActionListener(e -> loadSelectedEnvironment());

        newEnvButton = new JButton("+");
        newEnvButton.setToolTipText("Create a new environment");
        newEnvButton.setPreferredSize(new Dimension(40, 28));
        newEnvButton.addActionListener(e -> handleNewEnvironment());

        panel.add(environmentDropdown, BorderLayout.CENTER);
        panel.add(newEnvButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createKeyValuePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIManager.getColor("Panel.background"));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel keyHeader = new JLabel("Key");
        keyHeader.setFont(keyHeader.getFont().deriveFont(Font.BOLD, 11f));
        keyHeader.setForeground(UIManager.getColor("Label.foreground"));
        keyHeader.setPreferredSize(new Dimension(100, 20));

        JLabel valueHeader = new JLabel("Value");
        valueHeader.setFont(valueHeader.getFont().deriveFont(Font.BOLD, 11f));
        valueHeader.setForeground(UIManager.getColor("Label.foreground"));

        JLabel spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(32, 20));

        headerPanel.add(keyHeader, BorderLayout.WEST);
        headerPanel.add(valueHeader, BorderLayout.CENTER);
        headerPanel.add(spacer, BorderLayout.EAST);

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(4));

        // Key-value input rows container with scroll
        keyValueRowsContainer = new JPanel();
        keyValueRowsContainer.setLayout(new BoxLayout(keyValueRowsContainer, BoxLayout.Y_AXIS));
        keyValueRowsContainer.setBackground(UIManager.getColor("Panel.background"));

        for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
            addKeyValueRow();
        }

        JScrollPane scrollPane = new JScrollPane(keyValueRowsContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(0, 250));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(6));

        // Button panel with Add Variable and Save buttons on same row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addRowButton = new JButton("+ Add Variable");
        addRowButton.setPreferredSize(new Dimension(140, 28));
        addRowButton.addActionListener(e -> {
            addKeyValueRow();
            keyValueRowsContainer.revalidate();
            keyValueRowsContainer.repaint();
        });

        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(80, 28));
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(addRowButton);
        buttonPanel.add(saveButton);

        panel.add(buttonPanel);

        return panel;
    }

    private void addKeyValueRow() {
        JPanel rowPanel = new JPanel(new BorderLayout(4, 0));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        rowPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        rowPanel.setBackground(UIManager.getColor("Panel.background"));

        JTextField keyField = new JTextField();
        keyField.setPreferredSize(new Dimension(100, 24));
        keyField.setMinimumSize(new Dimension(100, 24));
        keyField.setMaximumSize(new Dimension(100, 24));

        JTextField valueField = new JTextField();

        JButton removeButton = new JButton("×");
        removeButton.setPreferredSize(new Dimension(32, 24));
        removeButton.setToolTipText("Remove this variable");
        removeButton.setFont(removeButton.getFont().deriveFont(16f));
        removeButton.setMargin(new Insets(0, 0, 0, 0));

        keyFields.add(keyField);
        valueFields.add(valueField);
        removeButtons.add(removeButton);

        removeButton.addActionListener(e -> {
            // Disable button immediately to prevent double-clicks
            removeButton.setEnabled(false);

            System.out.println("Remove button clicked. Lists before removal:");
            System.out.println("  keyFields.size() = " + keyFields.size());
            System.out.println("  valueFields.size() = " + valueFields.size());
            System.out.println("  removeButtons.size() = " + removeButtons.size());
            System.out.println("  keyValueRowsContainer.getComponentCount() = " + keyValueRowsContainer.getComponentCount());

            int index = removeButtons.indexOf(removeButton);
            System.out.println("  Index of clicked button: " + index);

            if (index >= 0 && index < keyFields.size()) {
                // Remove from lists
                keyFields.remove(index);
                valueFields.remove(index);
                removeButtons.remove(index);

                System.out.println("Lists after removal:");
                System.out.println("  keyFields.size() = " + keyFields.size());
                System.out.println("  valueFields.size() = " + valueFields.size());
                System.out.println("  removeButtons.size() = " + removeButtons.size());

                // Remove from UI
                keyValueRowsContainer.remove(rowPanel);

                System.out.println("  keyValueRowsContainer.getComponentCount() = " + keyValueRowsContainer.getComponentCount());

                keyValueRowsContainer.revalidate();
                keyValueRowsContainer.repaint();
            }
        });

        rowPanel.add(keyField, BorderLayout.WEST);
        rowPanel.add(valueField, BorderLayout.CENTER);
        rowPanel.add(removeButton, BorderLayout.EAST);

        keyValueRowsContainer.add(rowPanel);
    }

    private void loadEnvironmentsFromDisk() {
        Map<String, Environment> environments = environmentService.loadEnvironments();

        environmentDropdown.removeAllItems();

        if (environments.isEmpty()) {
            String[] defaultEnvs = {"Development", "Staging", "Production", "Testing", "Local"};
            for (String env : defaultEnvs) {
                environmentDropdown.addItem(env);
            }
            environmentDropdown.setSelectedItem("Development");
        } else {
            for (String envName : environments.keySet()) {
                environmentDropdown.addItem(envName);
            }

            String currentEnv = appState.getSelectedEnvironment();
            if (environments.containsKey(currentEnv)) {
                environmentDropdown.setSelectedItem(currentEnv);
            }
        }

        loadSelectedEnvironment();
    }

    private void loadSelectedEnvironment() {
        String selectedName = (String) environmentDropdown.getSelectedItem();
        if (selectedName == null) return;

        // Update app state
        appState.setSelectedEnvironment(selectedName);

        Environment env = environmentService.loadEnvironment(selectedName);

        keyValueRowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();

        if (env != null && !env.getVariables().isEmpty()) {
            // Update app state with the loaded environment variables
            appState.setEnvironmentVariables(env.getVariables());

            for (Map.Entry<String, String> entry : env.getVariables().entrySet()) {
                addKeyValueRow();
                int lastIndex = keyFields.size() - 1;
                keyFields.get(lastIndex).setText(entry.getKey());
                valueFields.get(lastIndex).setText(entry.getValue());
            }
        } else {
            // Clear environment variables in app state if environment is empty
            appState.setEnvironmentVariables(new HashMap<>());

            for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
                addKeyValueRow();
            }
        }

        keyValueRowsContainer.revalidate();
        keyValueRowsContainer.repaint();
    }

    private void handleSave() {
        appState.setStatus("Saving environment...", "🔵");

        String selectedEnvironment = (String) environmentDropdown.getSelectedItem();
        if (selectedEnvironment == null || selectedEnvironment.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select or create an environment first.",
                "No Environment Selected",
                JOptionPane.WARNING_MESSAGE);
            appState.setStatusError("No environment selected");
            return;
        }

        Map<String, String> keyValuePairs = new HashMap<>();

        for (int i = 0; i < keyFields.size(); i++) {
            String key = keyFields.get(i).getText().trim();
            String value = valueFields.get(i).getText().trim();

            if (!key.isEmpty() && !value.isEmpty()) {
                keyValuePairs.put(key, value);
            }
        }

        try {
            Environment environment = new Environment(selectedEnvironment, keyValuePairs);
            environmentService.saveEnvironment(environment);

            appState.setEnvironmentVariables(keyValuePairs);
            appState.setStatusSuccess("Environment '" + selectedEnvironment + "' saved");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to save environment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);

            appState.setStatusError("Failed to save environment");
        }
    }

    private void handleNewEnvironment() {
        String newEnvName = JOptionPane.showInputDialog(
            this,
            "Enter new environment name:",
            "New Environment",
            JOptionPane.PLAIN_MESSAGE
        );

        if (newEnvName != null && !newEnvName.trim().isEmpty()) {
            newEnvName = newEnvName.trim();

            if (environmentService.environmentExists(newEnvName)) {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "Environment '" + newEnvName + "' already exists. Do you want to edit it?",
                    "Environment Exists",
                    JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    environmentDropdown.setSelectedItem(newEnvName);
                }
            } else {
                environmentDropdown.addItem(newEnvName);
                environmentDropdown.setSelectedItem(newEnvName);
            }
        }
    }

    public void refresh() {
        loadEnvironmentsFromDisk();
    }
}
