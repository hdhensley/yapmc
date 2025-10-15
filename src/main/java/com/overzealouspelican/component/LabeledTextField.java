package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable component for a labeled text input field with IntelliJ-style appearance.
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
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(UIManager.getColor("Panel.background"));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setForeground(UIManager.getColor("Label.foreground"));

        textField.setToolTipText(tooltipText);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        textField.setPreferredSize(new Dimension(0, 28));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(label);
        add(Box.createVerticalStrut(4));
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
