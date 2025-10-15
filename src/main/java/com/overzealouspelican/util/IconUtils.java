package com.overzealouspelican.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class IconUtils {

    /**
     * Loads an icon from the resources/icons directory
     * @param iconName the name of the icon file (e.g., "save.png")
     * @param size the desired size (will scale the icon)
     * @return ImageIcon or null if not found
     */
    public static ImageIcon loadIcon(String iconName, int size) {
        try {
            URL iconURL = IconUtils.class.getResource("/icons/" + iconName);
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                // Scale the icon to the desired size
                Image scaledImage = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconName);
        }
        return null;
    }

    /**
     * Loads an icon with default 16px size
     */
    public static ImageIcon loadIcon(String iconName) {
        return loadIcon(iconName, 16);
    }

    /**
     * Creates a colored icon for simple shapes (useful for status indicators)
     */
    public static ImageIcon createColorIcon(Color color, int size) {
        Image image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(2, 2, size - 4, size - 4);
        g2d.dispose();
        return new ImageIcon(image);
    }
}
