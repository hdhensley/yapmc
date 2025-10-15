package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable component for URL input with HTTP method selector.
 * Follows Single Responsibility Principle - manages URL and HTTP method input.
 */
public class UrlWithMethodInput extends JPanel {

    private final JTextField urlField;
    private final JComboBox<String> httpMethodDropdown;
    private final JLabel label;

    public UrlWithMethodInput() {
        this.label = new JLabel("URL");
        this.urlField = new JTextField();

        String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        this.httpMethodDropdown = new JComboBox<>(httpMethods);

        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(UIManager.getColor("Panel.background"));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel for URL field and dropdown on same line
        JPanel urlInputPanel = new JPanel(new BorderLayout(10, 0));
        urlInputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        urlInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        urlInputPanel.setBackground(UIManager.getColor("Panel.background"));

        urlField.setToolTipText("Enter the URL for this call");
        httpMethodDropdown.setPreferredSize(new Dimension(100, 25));
        httpMethodDropdown.setToolTipText("Select HTTP method");

        urlInputPanel.add(urlField, BorderLayout.CENTER);
        urlInputPanel.add(httpMethodDropdown, BorderLayout.EAST);

        add(label);
        add(Box.createVerticalStrut(5));
        add(urlInputPanel);
    }

    public String getUrl() {
        return urlField.getText().trim();
    }

    public void setUrl(String url) {
        urlField.setText(url);
    }

    public String getHttpMethod() {
        return (String) httpMethodDropdown.getSelectedItem();
    }

    public void setHttpMethod(String httpMethod) {
        httpMethodDropdown.setSelectedItem(httpMethod);
    }
}

