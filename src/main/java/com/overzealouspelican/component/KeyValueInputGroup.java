package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable component for key-value input pairs with IntelliJ-style appearance.
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
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        add(label);
        add(Box.createVerticalStrut(8));

        // Column labels
        JPanel columnLabelsPanel = new JPanel(new BorderLayout(8, 0));
        columnLabelsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        columnLabelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnLabelsPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel keyLabel = new JLabel("Key");
        keyLabel.setFont(keyLabel.getFont().deriveFont(Font.PLAIN, 11f));
        keyLabel.setForeground(UIManager.getColor("Label.foreground"));
        keyLabel.setPreferredSize(new Dimension(180, 20));

        JLabel valueLabel = new JLabel("Value");
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, 11f));
        valueLabel.setForeground(UIManager.getColor("Label.foreground"));

        JLabel spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(36, 20));

        columnLabelsPanel.add(keyLabel, BorderLayout.WEST);
        columnLabelsPanel.add(valueLabel, BorderLayout.CENTER);
        columnLabelsPanel.add(spacer, BorderLayout.EAST);

        add(columnLabelsPanel);
        add(Box.createVerticalStrut(4));

        // Rows container with scroll
        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setBackground(UIManager.getColor("Panel.background"));
        addRow(); // Start with 1 row

        JScrollPane scrollPane = new JScrollPane(rowsContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane);
        add(Box.createVerticalStrut(8));

        // Add Row button
        JButton addRowButton = new JButton(addButtonText);
        addRowButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addRowButton.setMaximumSize(new Dimension(150, 28));
        addRowButton.addActionListener(e -> {
            addRow();
            rowsContainer.revalidate();
            rowsContainer.repaint();
        });
        add(addRowButton);
    }

    private void addRow() {
        JPanel rowPanel = new JPanel(new BorderLayout(4, 0));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        rowPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        rowPanel.setBackground(UIManager.getColor("Panel.background"));

        JTextField keyField = new JTextField();
        keyField.setPreferredSize(new Dimension(180, 24));
        keyField.setMinimumSize(new Dimension(180, 24));
        keyField.setMaximumSize(new Dimension(180, 24));

        JTextField valueField = new JTextField();

        JButton removeButton = new JButton("×");
        removeButton.setPreferredSize(new Dimension(36, 24));
        removeButton.setToolTipText(removeTooltip);
        removeButton.setFont(removeButton.getFont().deriveFont(16f));
        removeButton.setMargin(new Insets(0, 0, 0, 0));

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

            Component[] components = rowsContainer.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == rowPanel) {
                    rowsContainer.remove(i);
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
    }

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

    public void setKeyValuePairs(Map<String, String> pairs) {
        rowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();

        if (pairs.isEmpty()) {
            addRow();
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
