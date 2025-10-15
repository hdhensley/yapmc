package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable component for key-value input pairs with add/remove functionality.
 * Follows Single Responsibility Principle - manages only key-value input rows.
 */
public class KeyValueInputGroup extends JPanel {

    private final String groupLabel;
    private final String addButtonText;
    private final String removeTooltip;
    private final List<JTextField> keyFields;
    private final List<JTextField> valueFields;
    private final List<JButton> removeButtons;
    private final JPanel rowsContainer;

    public KeyValueInputGroup(String groupLabel, String addButtonText, String removeTooltip) {
        this.groupLabel = groupLabel;
        this.addButtonText = addButtonText;
        this.removeTooltip = removeTooltip;
        this.keyFields = new ArrayList<>();
        this.valueFields = new ArrayList<>();
        this.removeButtons = new ArrayList<>();
        this.rowsContainer = new JPanel();

        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(UIManager.getColor("Panel.background"));

        // Group label
        JLabel label = new JLabel(groupLabel);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(5));

        // Column labels with proper sizing
        JPanel columnLabelsPanel = new JPanel(new BorderLayout(10, 0));
        columnLabelsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        columnLabelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnLabelsPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel keyLabel = new JLabel("Key");
        keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD, 11f));
        keyLabel.setPreferredSize(new Dimension(200, 20));

        JLabel valueLabel = new JLabel("Value");
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 11f));

        columnLabelsPanel.add(keyLabel, BorderLayout.WEST);
        columnLabelsPanel.add(valueLabel, BorderLayout.CENTER);
        // Add spacer for the remove button column
        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new Dimension(45, 20));
        columnLabelsPanel.add(spacer, BorderLayout.EAST);

        add(columnLabelsPanel);
        add(Box.createVerticalStrut(5));

        // Rows container with scroll
        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        addRow(); // Start with 1 row

        JScrollPane scrollPane = new JScrollPane(rowsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(scrollPane);
        add(Box.createVerticalStrut(5));

        // Add Row button
        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        addButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButtonPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton addRowButton = new JButton(addButtonText);
        addRowButton.addActionListener(e -> {
            addRow();
            rowsContainer.revalidate();
            rowsContainer.repaint();
        });
        addButtonPanel.add(addRowButton);
        add(addButtonPanel);
    }

    private void addRow() {
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
        removeButton.setToolTipText(removeTooltip);

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
            Component[] components = rowsContainer.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == rowPanel) {
                    rowsContainer.remove(i); // Remove the row panel
                    // Remove the following vertical strut if it exists
                    if (i < rowsContainer.getComponentCount()) {
                        rowsContainer.remove(i);
                    }
                    break;
                }
            }

            rowsContainer.revalidate();
            rowsContainer.repaint();
        });

        rowPanel.add(keyField, BorderLayout.WEST);
        rowPanel.add(valueField, BorderLayout.CENTER);
        rowPanel.add(removeButton, BorderLayout.EAST);

        rowsContainer.add(rowPanel);
        rowsContainer.add(Box.createVerticalStrut(5));
    }

    /**
     * Get all non-empty key-value pairs as a Map.
     */
    public Map<String, String> getKeyValuePairs() {
        Map<String, String> pairs = new HashMap<>();
        for (int i = 0; i < keyFields.size(); i++) {
            String key = keyFields.get(i).getText().trim();
            String value = valueFields.get(i).getText().trim();

            if (!key.isEmpty() && !value.isEmpty()) {
                pairs.put(key, value);
            }
        }
        return pairs;
    }

    /**
     * Set key-value pairs from a Map.
     */
    public void setKeyValuePairs(Map<String, String> pairs) {
        // Clear existing rows
        rowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();

        // Add rows for each pair
        if (pairs.isEmpty()) {
            addRow(); // At least one empty row
        } else {
            for (Map.Entry<String, String> entry : pairs.entrySet()) {
                addRow();
                int lastIndex = keyFields.size() - 1;
                keyFields.get(lastIndex).setText(entry.getKey());
                valueFields.get(lastIndex).setText(entry.getValue());
            }
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    /**
     * Clear all input fields.
     */
    public void clear() {
        rowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();
        addRow();
        rowsContainer.revalidate();
        rowsContainer.repaint();
    }
}
