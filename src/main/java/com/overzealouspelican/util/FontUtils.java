package com.overzealouspelican.util;

import java.awt.*;

public class FontUtils {

    /**
     * Get all available system fonts
     */
    public static String[] getAvailableFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getAvailableFontFamilyNames();
    }

    /**
     * Print all available fonts to console
     */
    public static void printAvailableFonts() {
        System.out.println("Available fonts on this system:");
        String[] fonts = getAvailableFonts();
        for (int i = 0; i < fonts.length; i++) {
            System.out.println((i + 1) + ". " + fonts[i]);
        }
        System.out.println("Total fonts available: " + fonts.length);
    }

    /**
     * Common cross-platform fonts that are usually available
     */
    public static class CommonFonts {
        // Serif fonts
        public static final Font SERIF_PLAIN_12 = new Font(Font.SERIF, Font.PLAIN, 12);
        public static final Font SERIF_BOLD_12 = new Font(Font.SERIF, Font.BOLD, 12);
        public static final Font SERIF_ITALIC_12 = new Font(Font.SERIF, Font.ITALIC, 12);

        // Sans-serif fonts
        public static final Font SANS_SERIF_PLAIN_12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        public static final Font SANS_SERIF_BOLD_12 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        public static final Font SANS_SERIF_ITALIC_12 = new Font(Font.SANS_SERIF, Font.ITALIC, 12);

        // Monospace fonts
        public static final Font MONOSPACED_PLAIN_12 = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        public static final Font MONOSPACED_BOLD_12 = new Font(Font.MONOSPACED, Font.BOLD, 12);

        // Common system fonts (may not be available on all systems)
        public static final Font ARIAL_12 = new Font("Arial", Font.PLAIN, 12);
        public static final Font HELVETICA_12 = new Font("Helvetica", Font.PLAIN, 12);
        public static final Font TIMES_NEW_ROMAN_12 = new Font("Times New Roman", Font.PLAIN, 12);
        public static final Font COURIER_NEW_12 = new Font("Courier New", Font.PLAIN, 12);
        public static final Font VERDANA_12 = new Font("Verdana", Font.PLAIN, 12);
    }

    /**
     * Create a font with fallback if the preferred font isn't available
     */
    public static Font createFont(String preferredFont, int style, int size) {
        Font font = new Font(preferredFont, style, size);
        // Check if font was created successfully (not substituted)
        if (!font.getFamily().equals(preferredFont)) {
            System.out.println("Font '" + preferredFont + "' not available, using fallback: " + font.getFamily());
        }
        return font;
    }
}
