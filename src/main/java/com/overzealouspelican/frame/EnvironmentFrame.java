package com.overzealouspelican.frame;

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
 * Environment frame that opens from the control panel.
 */
public class EnvironmentFrame extends JFrame {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private static final int INITIAL_KEY_VALUE_ROWS = 3;

    private JComboBox<String> environmentDropdown;
    private List<JTextField> keyFields;
    private List<JTextField> valueFields;
    private List<JButton> removeButtons;
    private JPanel keyValueRowsContainer;
    private ApplicationState appState;
    private EnvironmentService environmentService;

    public EnvironmentFrame() {
        keyFields = new ArrayList<>();
        valueFields = new ArrayList<>();
        removeButtons = new ArrayList<>();
        appState = ApplicationState.getInstance();
        environmentService = new EnvironmentService();
        initializeFrame();
        addComponents();
        loadEnvironmentsFromDisk();
    }

    private void initializeFrame() {
        setTitle("Environment");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }

    private void addComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Dropdown section
        mainPanel.add(createDropdownPanel());

        // First divider
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createDivider());
        mainPanel.add(Box.createVerticalStrut(10));

        // Key-value pairs section
        mainPanel.add(createKeyValuePanel());

        // Second divider
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createDivider());
        mainPanel.add(Box.createVerticalStrut(10));

        // Button section
        mainPanel.add(createButtonPanel());

        add(mainPanel, BorderLayout.CENTER);

        // Pack to fit components
        pack();
        setMinimumSize(new Dimension(WINDOW_WIDTH, 400));
    }

    private JPanel createDropdownPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        environmentDropdown = new JComboBox<>();
        environmentDropdown.addActionListener(e -> loadSelectedEnvironment());

        JButton newEnvButton = new JButton("+ New");
        newEnvButton.setToolTipText("Create a new environment");
        newEnvButton.addActionListener(e -> handleNewEnvironment());

        panel.add(environmentDropdown, BorderLayout.CENTER);
        panel.add(newEnvButton, BorderLayout.EAST);

        return panel;
    }

    private JSeparator createDivider() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(UIManager.getColor("Component.borderColor"));
        return separator;
    }

    private JPanel createKeyValuePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Header with proper sizing
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel keyHeader = new JLabel("Key");
        keyHeader.setFont(keyHeader.getFont().deriveFont(Font.BOLD));
        keyHeader.setPreferredSize(new Dimension(200, 20));

        JLabel valueHeader = new JLabel("Value");
        valueHeader.setFont(valueHeader.getFont().deriveFont(Font.BOLD));

        // Add spacer for remove button column
        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new Dimension(45, 20));

        headerPanel.add(keyHeader, BorderLayout.WEST);
        headerPanel.add(valueHeader, BorderLayout.CENTER);
        headerPanel.add(spacer, BorderLayout.EAST);

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(5));

        // Key-value input rows container with scroll
        keyValueRowsContainer = new JPanel();
        keyValueRowsContainer.setLayout(new BoxLayout(keyValueRowsContainer, BoxLayout.Y_AXIS));
        for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
            addKeyValueRow();
        }

        JScrollPane scrollPane = new JScrollPane(keyValueRowsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(5));

        // Add Row button
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JButton addRowButton = new JButton("+ Add Row");
        addRowButton.addActionListener(e -> {
            addKeyValueRow();
            keyValueRowsContainer.revalidate();
            keyValueRowsContainer.repaint();
        });
        addButtonPanel.add(addRowButton);
        panel.add(addButtonPanel);

        return panel;
    }

    private void addKeyValueRow() {
        JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Create key field with fixed width
        JTextField keyField = new JTextField();
        keyField.setPreferredSize(new Dimension(200, 25));
        keyField.setMinimumSize(new Dimension(200, 25));
        keyField.setMaximumSize(new Dimension(200, 25));

        // Create value field that takes remaining space
        JTextField valueField = new JTextField();

        // Create remove button
        JButton removeButton = new JButton("✕");
        removeButton.setPreferredSize(new Dimension(45, 25));
        removeButton.setToolTipText("Remove this row");

        keyFields.add(keyField);
        valueFields.add(valueField);
        removeButtons.add(removeButton);

        removeButton.addActionListener(e -> {
            int index = removeButtons.indexOf(removeButton);
            if (index >= 0) {
                keyFields.remove(index);
                valueFields.remove(index);
                removeButtons.remove(index);
            }

            // Find and remove the row panel from the container
            Component[] components = keyValueRowsContainer.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == rowPanel) {
                    keyValueRowsContainer.remove(i); // Remove the row panel
                    // Remove the following vertical strut if it exists
                    if (i < keyValueRowsContainer.getComponentCount()) {
                        keyValueRowsContainer.remove(i);
                    }
                    break;
                }
            }

            keyValueRowsContainer.revalidate();
            keyValueRowsContainer.repaint();
        });

        rowPanel.add(keyField, BorderLayout.WEST);
        rowPanel.add(valueField, BorderLayout.CENTER);
        rowPanel.add(removeButton, BorderLayout.EAST);

        keyValueRowsContainer.add(rowPanel);
        keyValueRowsContainer.add(Box.createVerticalStrut(5));
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> handleCancel());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> handleSave());

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    /**
     * Load all environments from disk and populate the dropdown
     */
    private void loadEnvironmentsFromDisk() {
        Map<String, Environment> environments = environmentService.loadEnvironments();

        // Clear dropdown
        environmentDropdown.removeAllItems();

        // Add default environments if none exist
        if (environments.isEmpty()) {
            String[] defaultEnvs = {"Development", "Staging", "Production", "Testing", "Local"};
            for (String env : defaultEnvs) {
                environmentDropdown.addItem(env);
            }
            environmentDropdown.setSelectedItem("Development");
        } else {
            // Load saved environments
            for (String envName : environments.keySet()) {
                environmentDropdown.addItem(envName);
            }

            // Select the first one or the current app state environment
            String currentEnv = appState.getSelectedEnvironment();
            if (environments.containsKey(currentEnv)) {
                environmentDropdown.setSelectedItem(currentEnv);
            }
        }

        // Load the selected environment's variables
        loadSelectedEnvironment();
    }

    /**
     * Load the currently selected environment's variables into the UI
     */
    private void loadSelectedEnvironment() {
        String selectedName = (String) environmentDropdown.getSelectedItem();
        if (selectedName == null) return;

        Environment env = environmentService.loadEnvironment(selectedName);

        // Clear existing rows
        keyValueRowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();

        if (env != null && !env.getVariables().isEmpty()) {
            // Load variables from saved environment
            for (Map.Entry<String, String> entry : env.getVariables().entrySet()) {
                addKeyValueRow();
                int lastIndex = keyFields.size() - 1;
                keyFields.get(lastIndex).setText(entry.getKey());
                valueFields.get(lastIndex).setText(entry.getValue());
            }
        } else {
            // Add empty rows
            for (int i = 0; i < INITIAL_KEY_VALUE_ROWS; i++) {
                addKeyValueRow();
            }
        }

        keyValueRowsContainer.revalidate();
        keyValueRowsContainer.repaint();
    }

    private void handleSave() {
        // Update status to show saving
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
            // Create and save the environment
            Environment environment = new Environment(selectedEnvironment, keyValuePairs);
            environmentService.saveEnvironment(environment);

            System.out.println("Saved environment: " + selectedEnvironment);
            System.out.println("Key-Value pairs: " + keyValuePairs);
            System.out.println("Saved to: " + environmentService.getEnvironmentsFilePath());

            // Update ApplicationState with the new environment variables immediately
            appState.setEnvironmentVariables(keyValuePairs);

            // Update status to success (removed popup dialog)
            appState.setStatusSuccess("Environment '" + selectedEnvironment + "' saved");
        } catch (Exception e) {
            System.err.println("Failed to save environment: " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(this,
                "Failed to save environment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);

            appState.setStatusError("Failed to save environment");
        }
    }

    private void handleCancel() {
        dispose();
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

            // Check if environment already exists
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

    /**
     * Display the frame
     */
    public void display() {
        setLocationRelativeTo(null); // Center on screen after pack()
        setVisible(true);
    }
}
