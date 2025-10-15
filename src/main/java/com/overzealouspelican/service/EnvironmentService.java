package com.overzealouspelican.service;

import com.overzealouspelican.model.Environment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for persisting environments to JSON files on the local filesystem.
 * Follows Single Responsibility Principle - handles only environment persistence.
 */
public class EnvironmentService {

    private static final String APP_DIR_NAME = ".yapmc";
    private static final String ENVIRONMENTS_FILE = "environments.json";
    private final Gson gson;
    private final Path dataDirectory;
    private final Path environmentsFile;

    public EnvironmentService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataDirectory = getDataDirectory();
        this.environmentsFile = dataDirectory.resolve(ENVIRONMENTS_FILE);
        ensureDataDirectoryExists();
    }

    /**
     * Get the application data directory based on the OS
     */
    private Path getDataDirectory() {
        // Check if custom storage location is configured
        String customLocation = com.overzealouspelican.panel.SettingsEditorPanel.getStorageLocation();
        if (customLocation != null && !customLocation.isEmpty()) {
            return Paths.get(customLocation);
        }

        // Fall back to default location
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows: %APPDATA%\YAPMC
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "YAPMC");
            }
            return Paths.get(userHome, "AppData", "Roaming", "YAPMC");
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/YAPMC
            return Paths.get(userHome, "Library", "Application Support", "YAPMC");
        } else {
            // Linux/Unix: ~/.yapmc
            return Paths.get(userHome, APP_DIR_NAME);
        }
    }

    /**
     * Ensure the data directory exists
     */
    private void ensureDataDirectoryExists() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
                System.out.println("Created data directory: " + dataDirectory);
            }
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all environments from the JSON file
     */
    public Map<String, Environment> loadEnvironments() {
        if (!Files.exists(environmentsFile)) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(environmentsFile.toFile())) {
            Type type = new TypeToken<Map<String, Environment>>(){}.getType();
            Map<String, Environment> environments = gson.fromJson(reader, type);
            return environments != null ? environments : new HashMap<>();
        } catch (IOException e) {
            System.err.println("Failed to load environments: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Save all environments to the JSON file
     */
    public void saveEnvironments(Map<String, Environment> environments) throws IOException {
        try (FileWriter writer = new FileWriter(environmentsFile.toFile())) {
            gson.toJson(environments, writer);
            System.out.println("Saved environments to: " + environmentsFile);
        }
    }

    /**
     * Save a single environment
     */
    public void saveEnvironment(Environment environment) throws IOException {
        Map<String, Environment> environments = loadEnvironments();
        environments.put(environment.getName(), environment);
        saveEnvironments(environments);
    }

    /**
     * Load a specific environment by name
     */
    public Environment loadEnvironment(String name) {
        Map<String, Environment> environments = loadEnvironments();
        return environments.get(name);
    }

    /**
     * Delete an environment
     */
    public void deleteEnvironment(String name) throws IOException {
        Map<String, Environment> environments = loadEnvironments();
        environments.remove(name);
        saveEnvironments(environments);
    }

    /**
     * Check if an environment exists
     */
    public boolean environmentExists(String name) {
        Map<String, Environment> environments = loadEnvironments();
        return environments.containsKey(name);
    }

    /**
     * Get the path to the environments file
     */
    public String getEnvironmentsFilePath() {
        return environmentsFile.toString();
    }
}
