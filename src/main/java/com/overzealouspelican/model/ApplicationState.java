package com.overzealouspelican.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized application state following the Singleton pattern.
 * Uses the Observer pattern (PropertyChangeSupport) to notify listeners when state changes.
 * Follows SOLID principles - Single Responsibility for managing application state.
 */
public class ApplicationState {

    private static ApplicationState instance;
    private final PropertyChangeSupport propertyChangeSupport;

    // Application state properties
    private String selectedEnvironment;
    private Map<String, String> environmentVariables;
    private String statusMessage;
    private String statusIcon;

    // Property names for change events
    public static final String PROPERTY_SELECTED_ENVIRONMENT = "selectedEnvironment";
    public static final String PROPERTY_ENVIRONMENT_VARIABLES = "environmentVariables";
    public static final String PROPERTY_STATUS_MESSAGE = "statusMessage";
    public static final String PROPERTY_STATUS_ICON = "statusIcon";

    private ApplicationState() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.selectedEnvironment = "Development"; // Default value
        this.environmentVariables = new HashMap<>();
        this.statusMessage = "Ready";
        this.statusIcon = "🟢"; // Green circle emoji for ready status
    }

    /**
     * Get the singleton instance of ApplicationState
     */
    public static synchronized ApplicationState getInstance() {
        if (instance == null) {
            instance = new ApplicationState();
        }
        return instance;
    }

    /**
     * Add a listener to be notified when properties change
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Add a listener for a specific property
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove a property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fire a custom property change event
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    // Getters and Setters with property change notifications

    public String getSelectedEnvironment() {
        return selectedEnvironment;
    }

    public void setSelectedEnvironment(String newEnvironment) {
        String oldEnvironment = this.selectedEnvironment;
        this.selectedEnvironment = newEnvironment;
        propertyChangeSupport.firePropertyChange(PROPERTY_SELECTED_ENVIRONMENT, oldEnvironment, newEnvironment);
    }

    public Map<String, String> getEnvironmentVariables() {
        return new HashMap<>(environmentVariables); // Return a copy to prevent external modification
    }

    public void setEnvironmentVariables(Map<String, String> newVariables) {
        Map<String, String> oldVariables = this.environmentVariables;
        this.environmentVariables = new HashMap<>(newVariables);
        propertyChangeSupport.firePropertyChange(PROPERTY_ENVIRONMENT_VARIABLES, oldVariables, this.environmentVariables);
    }

    public void addEnvironmentVariable(String key, String value) {
        environmentVariables.put(key, value);
        propertyChangeSupport.firePropertyChange(PROPERTY_ENVIRONMENT_VARIABLES, null, environmentVariables);
    }

    public String getEnvironmentVariable(String key) {
        return environmentVariables.get(key);
    }

    // Status message getters and setters

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String newMessage) {
        String oldMessage = this.statusMessage;
        this.statusMessage = newMessage;
        propertyChangeSupport.firePropertyChange(PROPERTY_STATUS_MESSAGE, oldMessage, newMessage);
    }

    public String getStatusIcon() {
        return statusIcon;
    }

    public void setStatusIcon(String newIcon) {
        String oldIcon = this.statusIcon;
        this.statusIcon = newIcon;
        propertyChangeSupport.firePropertyChange(PROPERTY_STATUS_ICON, oldIcon, newIcon);
    }

    /**
     * Convenience method to set both status message and icon
     */
    public void setStatus(String message, String icon) {
        setStatusMessage(message);
        setStatusIcon(icon);
    }

    /**
     * Convenience methods for common status updates
     */
    public void setStatusReady() {
        setStatus("Ready", "🟢");
    }

    public void setStatusLoading() {
        setStatus("Loading...", "🔵");
    }

    public void setStatusError(String errorMessage) {
        setStatus("Error: " + errorMessage, "🔴");
    }

    public void setStatusSuccess(String message) {
        setStatus(message, "🟢");
    }

    public void setStatusWarning(String message) {
        setStatus(message, "🟠");
    }
}
