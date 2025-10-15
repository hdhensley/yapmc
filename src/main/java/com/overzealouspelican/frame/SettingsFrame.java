package com.overzealouspelican.frame;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.themes.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

/**
 * Settings frame for configuring application preferences.
 * Allows users to select FlatLaf themes and configure JSON storage location.
 */
public class SettingsFrame extends JFrame {

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsFrame.class);
    private static final String THEME_KEY = "theme";
    private static final String STORAGE_LOCATION_KEY = "storage_location";
    private static final String DEFAULT_THEME = "FlatLaf Light";

    private JComboBox<ThemeOption> themeComboBox;
    private JTextField storageLocationField;
    private JButton browseButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton resetButton;

    // Theme options with display name and class name
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

    public SettingsFrame() {
        setTitle("Settings");
        setSize(600, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initializeUI();
        loadSettings();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Content panel with form fields
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Theme selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        contentPanel.add(new JLabel("Theme:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        themeComboBox = new JComboBox<>(getAvailableThemes());
        contentPanel.add(themeComboBox, gbc);

        // Storage location
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        contentPanel.add(new JLabel("Storage Location:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        storageLocationField = new JTextField();
        storageLocationField.setToolTipText("Leave empty to use default location");
        contentPanel.add(storageLocationField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseForDirectory());
        contentPanel.add(browseButton, gbc);

        // Info label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        JLabel infoLabel = new JLabel("<html><i>Default storage location: " + getDefaultStorageLocation() + "</i></html>");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 11f));
        contentPanel.add(infoLabel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> resetToDefaults());
        buttonPanel.add(resetButton);

        buttonPanel.add(Box.createHorizontalStrut(20));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSettings());
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
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
        // Load theme
        String savedTheme = prefs.get(THEME_KEY, DEFAULT_THEME);
        for (int i = 0; i < themeComboBox.getItemCount(); i++) {
            if (themeComboBox.getItemAt(i).displayName.equals(savedTheme)) {
                themeComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Load storage location
        String savedLocation = prefs.get(STORAGE_LOCATION_KEY, "");
        storageLocationField.setText(savedLocation);
    }

    private void saveSettings() {
        // Save theme
        ThemeOption selectedTheme = (ThemeOption) themeComboBox.getSelectedItem();
        if (selectedTheme != null) {
            prefs.put(THEME_KEY, selectedTheme.displayName);

            // Apply theme
            try {
                UIManager.setLookAndFeel(selectedTheme.className);
                SwingUtilities.updateComponentTreeUI(this);

                // Update all open windows
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

        // Save storage location
        String storageLocation = storageLocationField.getText().trim();
        prefs.put(STORAGE_LOCATION_KEY, storageLocation);

        // Validate storage location if not empty
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

            JOptionPane.showMessageDialog(this,
                "Settings saved! Storage location will be used after application restart.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }

        dispose();
    }

    private void resetToDefaults() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Reset all settings to defaults?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // Reset theme to default
            for (int i = 0; i < themeComboBox.getItemCount(); i++) {
                if (themeComboBox.getItemAt(i).displayName.equals(DEFAULT_THEME)) {
                    themeComboBox.setSelectedIndex(i);
                    break;
                }
            }

            // Clear storage location
            storageLocationField.setText("");

            // Clear preferences
            prefs.remove(THEME_KEY);
            prefs.remove(STORAGE_LOCATION_KEY);

            JOptionPane.showMessageDialog(this,
                "Settings reset to defaults.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Get the configured storage location
     */
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
        ThemeOption[] themes = new SettingsFrame().getAvailableThemes();
        for (ThemeOption theme : themes) {
            if (theme.displayName.equals(displayName)) {
                return theme.className;
            }
        }
        return null;
    }

    public void display() {
        setVisible(true);
    }
}
