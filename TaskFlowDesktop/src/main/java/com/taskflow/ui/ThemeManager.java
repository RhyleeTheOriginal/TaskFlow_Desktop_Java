package com.taskflow.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;

public final class ThemeManager {
    private static boolean dark;
    private ThemeManager() {}

    public static void install(boolean useDark) {
        dark = useDark;
        try {
            if (dark) FlatDarkLaf.setup();
            else FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Could not install FlatLaf: " + ex.getMessage());
        }
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
    }

    public static boolean isDark() { return dark; }
}