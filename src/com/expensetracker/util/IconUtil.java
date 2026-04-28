package com.expensetracker.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;

public class IconUtil {

    private static final String ASSETS_DIR = "assets" + File.separator;

    public static ImageIcon load(String filename, int width, int height) {
        try {
            // Try loading from assets folder (relative to working directory)
            File file = new File(ASSETS_DIR + filename);
            if (file.exists()) {
                Image img = ImageIO.read(file);
                img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            // fall through to fallback
        }
        return null; // return null if not found, caller handles fallback
    }

    public static ImageIcon loadIcon(String filename) {
        return load(filename, 20, 20);
    }

    public static ImageIcon loadCategory(String filename) {
        return load(filename, 24, 24);
    }

    public static ImageIcon loadLogo(String filename) {
        return load(filename, 128, 128);
    }

    public static ImageIcon loadAppIcon() {
        return load("app_icon.png", 64, 64);
    }

    /** Returns icon for a category name */
    public static ImageIcon categoryIcon(String category) {
        switch (category) {
            case "Food & Dining":  return loadCategory("food.png");
            case "Transport":      return loadCategory("transport.png");
            case "Shopping":       return loadCategory("shopping.png");
            case "Entertainment":  return loadCategory("entertainment.png");
            case "Health":         return loadCategory("health.png");
            case "Education":      return loadCategory("education.png");
            case "Housing":        return loadCategory("housing.png");
            case "Utilities":      return loadCategory("utilities.png");
            case "Salary":         return loadCategory("salary.png");
            case "Freelance":      return loadCategory("freelance.png");
            case "Investment":     return loadCategory("investment.png");
            case "Gift":           return loadCategory("gift.png");
            default:               return loadCategory("other.png");
        }
    }

    /** Returns nav icon for sidebar */
    public static ImageIcon navIcon(String name) {
        switch (name) {
            case "Dashboard":    return loadIcon("dashboard.png");
            case "Monthly":      return loadIcon("monthly.png");
            case "Transactions": return loadIcon("transactions.png");
            case "Budget":       return loadIcon("budget.png");
            case "Add New":      return loadIcon("add.png");
            default:             return null;
        }
    }
}
