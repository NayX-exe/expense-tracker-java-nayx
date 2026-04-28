package com.expensetracker.ui;

import com.expensetracker.model.User;
import com.expensetracker.service.*;
import com.expensetracker.util.AppConfig;
import com.expensetracker.util.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class MainFrame extends JFrame {

    private final User user;
    private final UserService userService;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    private JPanel contentArea;
    private CardLayout contentLayout;
    private JButton[] navButtons;

    private DashboardPanel dashboardPanel;
    private MonthlyPanel monthlyPanel;
    private TransactionsPanel transactionsPanel;
    private BudgetPanel budgetPanel;
    private AddExpensePanel addExpensePanel;

    private static final String[] NAV       = {"Dashboard", "Monthly", "Transactions", "Budget", "Add New"};
    private static final String[] NAV_EMOJI = {"⊞", "📅", "📋", "💼", "＋"};

    public MainFrame(User user, UserService userService) {
        this.user = user;
        this.userService = userService;
        this.expenseService = new ExpenseService();
        this.budgetService  = new BudgetService();

        setTitle("Expense Tracker — " + user.getFullName());
        setSize(AppConfig.WINDOW_W, AppConfig.WINDOW_H);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        // Set app icon
        ImageIcon appIcon = IconUtil.loadAppIcon();
        if (appIcon != null) setIconImage(appIcon.getImage());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConfig.BG_DARK);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
        showPanel("Dashboard");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppConfig.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(AppConfig.BORDER_COLOR);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
        logoPanel.setOpaque(false);

        ImageIcon logoIcon = IconUtil.load("logo.png", 32, 32);
        if (logoIcon != null) {
            JLabel logoImg = new JLabel(logoIcon);
            logoPanel.add(logoImg);
        } else {
            JLabel logoEmoji = new JLabel("💰");
            logoEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            logoPanel.add(logoEmoji);
        }

        JPanel logoText = new JPanel(new GridLayout(2, 1));
        logoText.setOpaque(false);
        JLabel logoTitle = UIHelper.label("ExpenseTracker", 15, true);
        logoTitle.setForeground(AppConfig.TEXT_PRIMARY);
        JLabel logoSub = UIHelper.labelSecondary("Personal Finance", 11);
        logoText.add(logoTitle);
        logoText.add(logoSub);
        logoPanel.add(logoText);

        // Nav buttons
        JPanel navPanel = new JPanel(new GridLayout(NAV.length, 1, 0, 4));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        navButtons = new JButton[NAV.length];
        for (int i = 0; i < NAV.length; i++) {
            navButtons[i] = createNavButton(NAV[i], NAV_EMOJI[i], i == NAV.length - 1);
            final String panelName = NAV[i];
            navButtons[i].addActionListener(e -> showPanel(panelName));
            navPanel.add(navButtons[i]);
        }

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(buildUserPanel(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String label, String emoji, boolean isAccent) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = "true".equals(getClientProperty("selected"));
                boolean hov = getModel().isRollover();
                if (sel) {
                    g2.setColor(new Color(AppConfig.ACCENT_TEAL.getRed(),
                            AppConfig.ACCENT_TEAL.getGreen(), AppConfig.ACCENT_TEAL.getBlue(), 25));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(isAccent ? AppConfig.ACCENT_CORAL : AppConfig.ACCENT_TEAL);
                    g2.fill(new RoundRectangle2D.Float(0, 8, 3, getHeight() - 16, 3, 3));
                } else if (hov) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        b.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));

        // Try image icon first, fallback to emoji
        ImageIcon imgIcon = IconUtil.navIcon(label);
        if (imgIcon != null) {
            JLabel iconL = new JLabel(imgIcon);
            b.add(iconL);
            b.putClientProperty("iconLabel", iconL);
        } else {
            JLabel iconL = new JLabel(emoji);
            iconL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            iconL.setForeground(isAccent ? AppConfig.ACCENT_CORAL : AppConfig.TEXT_SECONDARY);
            b.add(iconL);
            b.putClientProperty("iconLabel", iconL);
        }

        JLabel textL = new JLabel(label);
        textL.setFont(AppConfig.fontPlain(14));
        textL.setForeground(AppConfig.TEXT_SECONDARY);
        b.add(textL);

        b.putClientProperty("textLabel", textL);
        b.putClientProperty("isAccent", isAccent);
        b.putClientProperty("emoji", emoji);
        b.putClientProperty("hasImage", imgIcon != null);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(196, 44));
        return b;
    }

    private JPanel buildUserPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppConfig.BORDER_COLOR),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // Avatar
        ImageIcon avatarIcon = IconUtil.load("avatar_default.png", 38, 38);
        JPanel avatar;
        if (avatarIcon != null) {
            avatar = new JPanel(new BorderLayout());
            avatar.setOpaque(false);
            avatar.add(new JLabel(avatarIcon));
        } else {
            avatar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppConfig.ACCENT_TEAL.darker());
                    g2.fillOval(0, 0, 38, 38);
                    g2.setColor(AppConfig.TEXT_PRIMARY);
                    g2.setFont(AppConfig.fontBold(16));
                    String init = user.getFullName().isEmpty() ? "?" :
                            String.valueOf(user.getFullName().charAt(0)).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(init, (38 - fm.stringWidth(init)) / 2,
                            (38 + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
        }
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(38, 38));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        info.add(UIHelper.label(user.getFullName(), 13, true));
        info.add(UIHelper.labelSecondary("@" + user.getUsername(), 11));

        JButton logoutBtn = UIHelper.ghostButton("↩");
        logoutBtn.setToolTipText("Logout");
        logoutBtn.addActionListener(e -> logout());

        p.add(avatar, BorderLayout.WEST);
        p.add(info, BorderLayout.CENTER);
        p.add(logoutBtn, BorderLayout.EAST);
        return p;
    }

    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentArea = new JPanel(contentLayout);
        contentArea.setBackground(AppConfig.BG_DARK);

        dashboardPanel    = new DashboardPanel(user, expenseService, budgetService, this);
        monthlyPanel      = new MonthlyPanel(user, expenseService);
        transactionsPanel = new TransactionsPanel(user, expenseService, this);
        budgetPanel       = new BudgetPanel(user, expenseService, budgetService);
        addExpensePanel   = new AddExpensePanel(user, expenseService, this);

        contentArea.add(dashboardPanel,    "Dashboard");
        contentArea.add(monthlyPanel,      "Monthly");
        contentArea.add(transactionsPanel, "Transactions");
        contentArea.add(budgetPanel,       "Budget");
        contentArea.add(addExpensePanel,   "Add New");

        return contentArea;
    }

    public void showPanel(String name) {
        contentLayout.show(contentArea, name);
        for (int i = 0; i < NAV.length; i++) {
            boolean sel = NAV[i].equals(name);
            navButtons[i].putClientProperty("selected", sel ? "true" : "false");
            JLabel textL = (JLabel) navButtons[i].getClientProperty("textLabel");
            JLabel iconL = (JLabel) navButtons[i].getClientProperty("iconLabel");
            boolean accent = Boolean.TRUE.equals(navButtons[i].getClientProperty("isAccent"));
            boolean hasImg = Boolean.TRUE.equals(navButtons[i].getClientProperty("hasImage"));
            if (sel) {
                textL.setForeground(AppConfig.TEXT_PRIMARY);
                textL.setFont(AppConfig.fontBold(14));
                if (!hasImg && iconL != null)
                    iconL.setForeground(accent ? AppConfig.ACCENT_CORAL : AppConfig.ACCENT_TEAL);
            } else {
                textL.setForeground(AppConfig.TEXT_SECONDARY);
                textL.setFont(AppConfig.fontPlain(14));
                if (!hasImg && iconL != null)
                    iconL.setForeground(AppConfig.TEXT_SECONDARY);
            }
            navButtons[i].repaint();
        }
        if ("Dashboard".equals(name))    dashboardPanel.refresh();
        if ("Monthly".equals(name))      monthlyPanel.refresh();
        if ("Transactions".equals(name)) transactionsPanel.refresh();
        if ("Budget".equals(name))       budgetPanel.refresh();
        if ("Add New".equals(name))      addExpensePanel.reset();
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(userService));
        }
    }
}