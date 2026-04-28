package com.expensetracker;

import com.expensetracker.service.UserService;
import com.expensetracker.ui.LoginFrame;
import com.expensetracker.util.AppConfig;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global UI defaults
        UIManager.put("OptionPane.background", AppConfig.BG_CARD);
        UIManager.put("Panel.background", AppConfig.BG_CARD);
        UIManager.put("OptionPane.messageForeground", AppConfig.TEXT_PRIMARY);
        UIManager.put("Button.background", AppConfig.ACCENT_TEAL);
        UIManager.put("Button.foreground", AppConfig.BG_DARK);
        UIManager.put("TextField.background", new java.awt.Color(0x1E, 0x2D, 0x3D));
        UIManager.put("TextField.foreground", AppConfig.TEXT_PRIMARY);
        UIManager.put("ComboBox.background", new java.awt.Color(0x1E, 0x2D, 0x3D));
        UIManager.put("ComboBox.foreground", AppConfig.TEXT_PRIMARY);

        UserService userService = new UserService();

        SwingUtilities.invokeLater(() -> new LoginFrame(userService));
    }
}