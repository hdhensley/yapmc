package com.overzealouspelican.frame;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.service.ApiCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

/**
 * Frame for importing API calls from HAR files.
 */
public class ImportFrame extends JFrame {

    private JTextArea filePathArea;
    private JButton browseButton;
    private JButton importButton;
    private JButton cancelButton;
    private JComboBox<String> entrySelector;
    private ApiCallService apiCallService;
    private Gson gson;
    private JsonObject harData;

    public ImportFrame() {
        this.apiCallService = new ApiCallService();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeFrame();
        addComponents();
    }

    private void initializeFrame() {
        setTitle("Import API Call from HAR");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
    }

    private void addComponents() {
        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(UIManager.getColor("Panel.background"));
        titleBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Import API Call from HAR File");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleBar.add(titleLabel, BorderLayout.WEST);

        add(titleBar, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        // File selection
        JLabel fileLabel = new JLabel("Select HAR file:");
        fileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(fileLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filePanel.setBackground(UIManager.getColor("Panel.background"));

        filePathArea = new JTextArea(1, 30);
        filePathArea.setEditable(false);
        filePathArea.setLineWrap(true);
        filePathArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        filePanel.add(filePathArea, BorderLayout.CENTER);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseFile());
        filePanel.add(browseButton, BorderLayout.EAST);

        contentPanel.add(filePanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Entry selector
        JLabel entryLabel = new JLabel("Select request to import:");
        entryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(entryLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        entrySelector = new JComboBox<>();
        entrySelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        entrySelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        entrySelector.setEnabled(false);
        contentPanel.add(entrySelector);

        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        importButton = new JButton("Import");
        importButton.setEnabled(false);
        importButton.addActionListener(e -> performImport());

        buttonPanel.add(cancelButton);
        buttonPanel.add(importButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HAR Files", "har"));
        fileChooser.setDialogTitle("Select HAR File");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathArea.setText(selectedFile.getAbsolutePath());
            loadHarFile(selectedFile);
        }
    }

    private void loadHarFile(File file) {
        try {
            String content = Files.readString(file.toPath());
            harData = gson.fromJson(content, JsonObject.class);

            // Parse HAR structure to get entries
            JsonObject log = harData.getAsJsonObject("log");
            if (log == null || !log.has("entries")) {
                throw new IllegalArgumentException("Invalid HAR format: missing log.entries");
            }

            JsonArray entries = log.getAsJsonArray("entries");
            if (entries.size() == 0) {
                throw new IllegalArgumentException("No entries found in HAR file");
            }

            // Populate entry selector with request URLs
            entrySelector.removeAllItems();
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();
                JsonObject request = entry.getAsJsonObject("request");
                String method = request.get("method").getAsString();
                String url = request.get("url").getAsString();

                // Truncate long URLs for display
                String displayUrl = url;
                if (displayUrl.length() > 80) {
                    displayUrl = displayUrl.substring(0, 80) + "...";
                }

                entrySelector.addItem(method + " " + displayUrl);
            }

            entrySelector.setEnabled(true);
            importButton.setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to load HAR file:\n" + e.getMessage(),
                "Load Error",
                JOptionPane.ERROR_MESSAGE);

            entrySelector.setEnabled(false);
            importButton.setEnabled(false);
        }
    }

    private void performImport() {
        String filePath = filePathArea.getText();
        if (filePath == null || filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select a file to import.",
                "No File Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedIndex = entrySelector.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a request to import.",
                "No Request Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get the selected entry
            JsonObject log = harData.getAsJsonObject("log");
            JsonArray entries = log.getAsJsonArray("entries");
            JsonObject entry = entries.get(selectedIndex).getAsJsonObject();
            JsonObject request = entry.getAsJsonObject("request");

            // Extract name from file name (without extension)
            File file = new File(filePath);
            String fileName = file.getName();
            String name = fileName.replaceFirst("[.][^.]+$", ""); // Remove extension

            // Convert HAR entry to ApiCall
            ApiCall apiCall = convertHarToApiCall(name, request);

            // Check if API call already exists
            if (apiCallService.apiCallExists(apiCall.getName())) {
                int overwrite = JOptionPane.showConfirmDialog(this,
                    "An API call with the name '" + apiCall.getName() + "' already exists.\nDo you want to overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Save the API call
            apiCallService.saveApiCall(apiCall);

            JOptionPane.showMessageDialog(this,
                "Successfully imported API call: " + apiCall.getName(),
                "Import Successful",
                JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to import API call:\n" + e.getMessage(),
                "Import Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private ApiCall convertHarToApiCall(String name, JsonObject request) {
        // Extract method and URL
        String method = request.get("method").getAsString();
        String url = request.get("url").getAsString();

        // Extract headers
        Map<String, String> headers = new HashMap<>();
        if (request.has("headers")) {
            JsonArray headersArray = request.getAsJsonArray("headers");
            for (JsonElement headerElement : headersArray) {
                JsonObject header = headerElement.getAsJsonObject();
                String headerName = header.get("name").getAsString();
                String headerValue = header.get("value").getAsString();

                // Skip some browser-specific headers
                if (!shouldSkipHeader(headerName)) {
                    headers.put(headerName, headerValue);
                }
            }
        }

        // Extract body (if present)
        Map<String, String> body = new HashMap<>();
        if (request.has("postData")) {
            JsonObject postData = request.getAsJsonObject("postData");
            if (postData.has("text")) {
                String bodyText = postData.get("text").getAsString();

                // Try to parse as JSON
                try {
                    JsonObject bodyJson = gson.fromJson(bodyText, JsonObject.class);
                    for (Map.Entry<String, JsonElement> entry : bodyJson.entrySet()) {
                        body.put(entry.getKey(), entry.getValue().getAsString());
                    }
                } catch (Exception e) {
                    // If not JSON, store as single entry
                    body.put("body", bodyText);
                }
            }
        }

        return new ApiCall(name, url, method, headers, body);
    }

    private boolean shouldSkipHeader(String headerName) {
        // Skip browser-specific and security headers that shouldn't be replicated
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("host") ||
               lowerName.equals("user-agent") ||
               lowerName.equals("accept-encoding") ||
               lowerName.equals("connection") ||
               lowerName.equals("referer") ||
               lowerName.equals("origin") ||
               lowerName.startsWith("sec-") ||
               lowerName.equals("dnt") ||
               lowerName.equals("priority") ||
               lowerName.equals("pragma") ||
               lowerName.equals("cache-control") ||
               lowerName.equals("te") ||
               lowerName.equals("content-length");
    }

    /**
     * Display the import frame
     */
    public void display() {
        setVisible(true);
        toFront();
        requestFocus();
    }
}

