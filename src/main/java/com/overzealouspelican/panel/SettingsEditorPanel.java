package com.overzealouspelican.panel;

import com.formdev.flatlaf.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * IntelliJ-style settings editor embedded in the sidebar.
 */
public class SettingsEditorPanel extends JPanel {

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsEditorPanel.class);
    private static final String THEME_KEY = "theme";
    private static final String STORAGE_LOCATION_KEY = "storage_location";
    private static final String DEFAULT_THEME = "FlatLaf IntelliJ";

    private JComboBox<ThemeOption> themeComboBox;
    private JTextField storageLocationField;
    private JButton browseButton;
    private JButton saveButton;
    private JButton resetButton;

    private static class ThemeOption {
        final String displayName;
        final String className;

        ThemeOption(String displayName, String className) {
            this.displayName = displayName;
            this.className = className;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public SettingsEditorPanel() {
        initializePanel();
        loadSettings();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Theme section
        mainPanel.add(createSectionLabel("Appearance"));
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(createThemePanel());
        mainPanel.add(Box.createVerticalStrut(16));

        // Storage section
        mainPanel.add(createSectionLabel("Storage"));
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(createStoragePanel());
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(createInfoLabel());
        mainPanel.add(Box.createVerticalStrut(16));

        // Buttons
        mainPanel.add(createButtonPanel());

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createThemePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("Theme:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        themeComboBox = new JComboBox<>(getAvailableThemes());
        themeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        themeComboBox.setPreferredSize(new Dimension(0, 28));
        themeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(themeComboBox);

        return panel;
    }

    private JPanel createStoragePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("Data Location:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new BorderLayout(8, 0));
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        fieldPanel.setBackground(UIManager.getColor("Panel.background"));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        storageLocationField = new JTextField();
        storageLocationField.setToolTipText("Leave empty to use default location");

        browseButton = new JButton("Browse...");
        browseButton.setPreferredSize(new Dimension(100, 28));
        browseButton.addActionListener(e -> browseForDirectory());

        fieldPanel.add(storageLocationField, BorderLayout.CENTER);
        fieldPanel.add(browseButton, BorderLayout.EAST);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(fieldPanel);

        return panel;
    }

    private JLabel createInfoLabel() {
        JLabel infoLabel = new JLabel("<html><i>Default: " + getDefaultStorageLocation() + "</i></html>");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 10f));
        infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return infoLabel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.setBackground(UIManager.getColor("Panel.background"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetToDefaults());

        saveButton = new JButton("Apply");
        saveButton.addActionListener(e -> saveSettings());

        panel.add(resetButton);
        panel.add(saveButton);

        return panel;
    }

    private ThemeOption[] getAvailableThemes() {
        return new ThemeOption[] {
            new ThemeOption("FlatLaf Light", FlatLightLaf.class.getName()),
            new ThemeOption("FlatLaf Dark", FlatDarkLaf.class.getName()),
            new ThemeOption("FlatLaf IntelliJ", FlatIntelliJLaf.class.getName()),
            new ThemeOption("FlatLaf Darcula", FlatDarculaLaf.class.getName())
        };
    }

    private String getDefaultStorageLocation() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return appData + File.separator + "YAPMC";
            }
            return userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "YAPMC";
        } else if (os.contains("mac")) {
            return userHome + File.separator + "Library" + File.separator + "Application Support" + File.separator + "YAPMC";
        } else {
            return userHome + File.separator + ".yapmc";
        }
    }

    private void browseForDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String currentLocation = storageLocationField.getText();
        if (!currentLocation.isEmpty()) {
            fileChooser.setCurrentDirectory(new File(currentLocation));
        }

        int result = fileChooser.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            storageLocationField.setText(selectedDir.getAbsolutePath());
        }
    }

    private void loadSettings() {
        String savedTheme = prefs.get(THEME_KEY, DEFAULT_THEME);
        for (int i = 0; i < themeComboBox.getItemCount(); i++) {
            if (themeComboBox.getItemAt(i).displayName.equals(savedTheme)) {
                themeComboBox.setSelectedIndex(i);
                break;
            }
        }

        String savedLocation = prefs.get(STORAGE_LOCATION_KEY, "");
        storageLocationField.setText(savedLocation);
    }

    private void saveSettings() {
        ThemeOption selectedTheme = (ThemeOption) themeComboBox.getSelectedItem();
        if (selectedTheme != null) {
            prefs.put(THEME_KEY, selectedTheme.displayName);

            try {
                UIManager.setLookAndFeel(selectedTheme.className);

                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }

                JOptionPane.showMessageDialog(this,
                    "Theme applied successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to apply theme: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String storageLocation = storageLocationField.getText().trim();
        prefs.put(STORAGE_LOCATION_KEY, storageLocation);

        if (!storageLocation.isEmpty()) {
            File storageDir = new File(storageLocation);
            if (!storageDir.exists()) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "The specified directory does not exist. Create it?",
                    "Create Directory",
                    JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    if (!storageDir.mkdirs()) {
                        JOptionPane.showMessageDialog(this,
                            "Failed to create directory: " + storageLocation,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
        }
    }

    private void resetToDefaults() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Reset all settings to defaults?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            for (int i = 0; i < themeComboBox.getItemCount(); i++) {
                if (themeComboBox.getItemAt(i).displayName.equals(DEFAULT_THEME)) {
                    themeComboBox.setSelectedIndex(i);
                    break;
                }
            }

            storageLocationField.setText("");
            prefs.remove(THEME_KEY);
            prefs.remove(STORAGE_LOCATION_KEY);

            JOptionPane.showMessageDialog(this,
                "Settings reset to defaults.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static String getStorageLocation() {
        return prefs.get(STORAGE_LOCATION_KEY, "");
    }

    /**
     * Load and apply the saved theme at application startup
     */
    public static void loadAndApplyTheme() {
        String savedThemeName = prefs.get(THEME_KEY, DEFAULT_THEME);
        String themeClassName = getThemeClassName(savedThemeName);

        if (themeClassName != null) {
            try {
                UIManager.setLookAndFeel(themeClassName);
            } catch (Exception ex) {
                System.err.println("Failed to load saved theme: " + ex.getMessage());
                // Fall back to default
                try {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                } catch (Exception e) {
                    System.err.println("Failed to load default theme");
                }
            }
        }
    }

    private static String getThemeClassName(String displayName) {
        ThemeOption[] themes = new SettingsEditorPanel().getAvailableThemes();
        for (ThemeOption theme : themes) {
            if (theme.displayName.equals(displayName)) {
                return theme.className;
            }
        }
        return null;
    }
}
