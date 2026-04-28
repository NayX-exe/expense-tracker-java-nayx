package com.expensetracker.util;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

public class AppConfig {

    // ─── Data directory ───────────────────────────────────────────────────────
    public static final String DATA_DIR;
    public static final String USERS_FILE;
    public static final String EXPENSES_FILE;
    public static final String BUDGETS_FILE;

    static {
        String home = System.getProperty("user.home");
        DATA_DIR = home + File.separator + "ExpenseTrackerData";
        USERS_FILE    = DATA_DIR + File.separator + "users.json";
        EXPENSES_FILE = DATA_DIR + File.separator + "expenses.json";
        BUDGETS_FILE  = DATA_DIR + File.separator + "budgets.json";
        new File(DATA_DIR).mkdirs();
    }

    // ─── Palette ──────────────────────────────────────────────────────────────
    public static final Color BG_DARK        = new Color(0x0F, 0x17, 0x23);
    public static final Color BG_CARD        = new Color(0x16, 0x21, 0x2E);
    public static final Color BG_SIDEBAR     = new Color(0x0B, 0x12, 0x1C);
    public static final Color ACCENT_TEAL    = new Color(0x00, 0xC8, 0xB4);
    public static final Color ACCENT_PURPLE  = new Color(0x7C, 0x3A, 0xFF);
    public static final Color ACCENT_CORAL   = new Color(0xFF, 0x6B, 0x6B);
    public static final Color ACCENT_GOLD    = new Color(0xFF, 0xBE, 0x0B);
    public static final Color TEXT_PRIMARY   = new Color(0xF0, 0xF4, 0xF8);
    public static final Color TEXT_SECONDARY = new Color(0x8A, 0x9B, 0xB0);
    public static final Color BORDER_COLOR   = new Color(0x22, 0x33, 0x44);
    public static final Color SUCCESS        = new Color(0x06, 0xD6, 0x8A);
    public static final Color DANGER         = new Color(0xFF, 0x4D, 0x6D);
    public static final Color INCOME_GREEN   = new Color(0x00, 0xE5, 0x96);
    public static final Color EXPENSE_RED    = new Color(0xFF, 0x5C, 0x5C);

    // ─── Category colors ──────────────────────────────────────────────────────
    public static Color getCategoryColor(String cat) {
        switch (cat) {
            case "Food & Dining":    return new Color(0xFF, 0x9F, 0x1C);
            case "Transport":        return new Color(0x2E, 0xC4, 0xB6);
            case "Shopping":         return new Color(0xFF, 0x6B, 0x6B);
            case "Entertainment":    return new Color(0x7C, 0x3A, 0xFF);
            case "Health":           return new Color(0x06, 0xD6, 0x8A);
            case "Education":        return new Color(0x00, 0xB4, 0xD8);
            case "Housing":          return new Color(0xFF, 0xBE, 0x0B);
            case "Utilities":        return new Color(0xFF, 0x70, 0xA6);
            case "Salary":           return new Color(0x00, 0xE5, 0x96);
            case "Freelance":        return new Color(0x4C, 0xC9, 0xF0);
            case "Investment":       return new Color(0xA8, 0xFF, 0x3E);
            default:                 return new Color(0xA0, 0xAE, 0xC0);
        }
    }

    // ─── Categories ───────────────────────────────────────────────────────────
    public static final String[] EXPENSE_CATEGORIES = {
        "Food & Dining", "Transport", "Shopping", "Entertainment",
        "Health", "Education", "Housing", "Utilities", "Other"
    };
    public static final String[] INCOME_CATEGORIES = {
        "Salary", "Freelance", "Investment", "Gift", "Other"
    };
    public static final String[] ALL_CATEGORIES = {
        "Food & Dining", "Transport", "Shopping", "Entertainment",
        "Health", "Education", "Housing", "Utilities",
        "Salary", "Freelance", "Investment", "Gift", "Other"
    };

    // ─── Fonts ────────────────────────────────────────────────────────────────
    public static Font font(int style, float size) {
        return new Font("Segoe UI", style, (int) size);
    }
    public static Font fontBold(float size)   { return font(Font.BOLD, size); }
    public static Font fontPlain(float size)  { return font(Font.PLAIN, size); }

    // ─── Window ───────────────────────────────────────────────────────────────
    public static final int WINDOW_W = 1200;
    public static final int WINDOW_H = 780;
    public static final int LOGIN_W  = 460;
    public static final int LOGIN_H  = 600;
}