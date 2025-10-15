package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable component for a labeled text input field.
 * Follows Single Responsibility Principle - manages a single labeled input.
 */
public class LabeledTextField extends JPanel {

    private final JTextField textField;
    private final JLabel label;

    public LabeledTextField(String labelText, String tooltipText) {
        this.label = new JLabel(labelText);
        this.textField = new JTextField();

        initializePanel(tooltipText);
    }

    private void initializePanel(String tooltipText) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(UIManager.getColor("Panel.background"));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setToolTipText(tooltipText);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(label);
        add(Box.createVerticalStrut(5));
        add(textField);
    }

    public String getText() {
        return textField.getText().trim();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public JTextField getTextField() {
        return textField;
    }
}

