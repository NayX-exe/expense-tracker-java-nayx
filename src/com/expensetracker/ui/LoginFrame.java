package com.expensetracker.ui;

import com.expensetracker.model.User;
import com.expensetracker.service.UserService;
import com.expensetracker.util.AppConfig;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class LoginFrame extends JFrame {

    private final UserService userService;
    private boolean showLogin = true;

    // Login fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // Register fields
    private JTextField regFullNameField, regUsernameField, regEmailField;
    private JPasswordField regPasswordField, regConfirmField;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    public LoginFrame(UserService userService) {
        this.userService = userService;
        setTitle("Expense Tracker — Sign In");
        setSize(AppConfig.LOGIN_W, AppConfig.LOGIN_H);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0B, 0x12, 0x1C),
                        getWidth(), getHeight(), new Color(0x0F, 0x1C, 0x2E));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Accent glow top-right
                RadialGradientPaint rgp = new RadialGradientPaint(
                        new Point(getWidth(), 0), 200,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0x00, 0xC8, 0xB4, 40), new Color(0, 0, 0, 0)});
                g2.setPaint(rgp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // Drag to move window
        addDragSupport(root);

        // Top bar
        JPanel topBar = buildTopBar();
        root.add(topBar, BorderLayout.NORTH);

        // Center content with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.add(buildLoginPanel(), "LOGIN");
        contentPanel.add(buildRegisterPanel(), "REGISTER");
        root.add(contentPanel, BorderLayout.CENTER);

        setContentPane(root);
        setShape(new RoundRectangle2D.Double(0, 0, AppConfig.LOGIN_W, AppConfig.LOGIN_H, 20, 20));
        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(16, 24, 0, 16));

        // Logo + title
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logo.setOpaque(false);
        JLabel icon = new JLabel("💰");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel title = UIHelper.label("ExpenseTracker", 20, true);
        title.setForeground(AppConfig.ACCENT_TEAL);
        logo.add(icon); logo.add(title);
        bar.add(logo, BorderLayout.WEST);

        // Close button
        JButton close = new JButton("✕");
        close.setFont(AppConfig.fontPlain(14));
        close.setForeground(AppConfig.TEXT_SECONDARY);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> System.exit(0));
        bar.add(close, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1;
        gbc.gridx = 0; gbc.gridy = 0;

        // Heading
        JLabel heading = UIHelper.label("Welcome back!", 26, true);
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(heading, gbc); gbc.gridy++;

        JLabel sub = UIHelper.labelSecondary("Sign in to your account", 14);
        gbc.insets = new Insets(0, 0, 24, 0);
        panel.add(sub, gbc); gbc.gridy++;

        // Fields
        gbc.insets = new Insets(4, 0, 4, 0);
        panel.add(UIHelper.labelSecondary("Username", 12), gbc); gbc.gridy++;
        loginUsernameField = UIHelper.styledField("Enter your username");
        panel.add(loginUsernameField, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Password", 12), gbc); gbc.gridy++;
        loginPasswordField = UIHelper.styledPasswordField("Enter your password");
        panel.add(loginPasswordField, gbc); gbc.gridy++;

        // Login button
        gbc.insets = new Insets(20, 0, 10, 0);
        JButton loginBtn = UIHelper.primaryButton("Sign In →");
        loginBtn.setPreferredSize(new Dimension(0, 48));
        loginBtn.addActionListener(e -> doLogin());
        panel.add(loginBtn, gbc); gbc.gridy++;

        // Switch to register
        gbc.insets = new Insets(4, 0, 0, 0);
        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        switchRow.add(UIHelper.labelSecondary("Don't have an account?", 13));
        JButton reg = UIHelper.ghostButton("Create one");
        reg.addActionListener(e -> switchCard("REGISTER"));
        switchRow.add(reg);
        panel.add(switchRow, gbc);

        // Enter key
        loginPasswordField.addActionListener(e -> doLogin());
        loginUsernameField.addActionListener(e -> loginPasswordField.requestFocus());

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.weightx = 1;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel heading = UIHelper.label("Create Account", 24, true);
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(heading, gbc); gbc.gridy++;

        JLabel sub = UIHelper.labelSecondary("Start tracking your expenses today", 13);
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(sub, gbc); gbc.gridy++;

        gbc.insets = new Insets(3, 0, 3, 0);
        panel.add(UIHelper.labelSecondary("Full Name", 12), gbc); gbc.gridy++;
        regFullNameField = UIHelper.styledField("Your full name");
        panel.add(regFullNameField, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Username", 12), gbc); gbc.gridy++;
        regUsernameField = UIHelper.styledField("Choose a username");
        panel.add(regUsernameField, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Email", 12), gbc); gbc.gridy++;
        regEmailField = UIHelper.styledField("your@email.com");
        panel.add(regEmailField, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Password", 12), gbc); gbc.gridy++;
        regPasswordField = UIHelper.styledPasswordField("Min 6 characters");
        panel.add(regPasswordField, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Confirm Password", 12), gbc); gbc.gridy++;
        regConfirmField = UIHelper.styledPasswordField("Repeat password");
        panel.add(regConfirmField, gbc); gbc.gridy++;

        gbc.insets = new Insets(14, 0, 8, 0);
        JButton regBtn = UIHelper.primaryButton("Create Account →");
        regBtn.setPreferredSize(new Dimension(0, 46));
        regBtn.addActionListener(e -> doRegister());
        panel.add(regBtn, gbc); gbc.gridy++;

        gbc.insets = new Insets(2, 0, 0, 0);
        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        switchRow.add(UIHelper.labelSecondary("Already have an account?", 13));
        JButton login = UIHelper.ghostButton("Sign in");
        login.addActionListener(e -> switchCard("LOGIN"));
        switchRow.add(login);
        panel.add(switchRow, gbc);

        return panel;
    }

    private void switchCard(String card) {
        cardLayout.show(contentPanel, card);
    }

    private void doLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields."); return;
        }
        User user = userService.login(username, password);
        if (user == null) { showError("Invalid username or password."); return; }
        openDashboard(user);
    }

    private void doRegister() {
        String name     = regFullNameField.getText().trim();
        String username = regUsernameField.getText().trim();
        String email    = regEmailField.getText().trim();
        String pass     = new String(regPasswordField.getPassword());
        String confirm  = new String(regConfirmField.getPassword());

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showError("All fields are required."); return;
        }
        if (!email.contains("@")) { showError("Enter a valid email."); return; }
        if (pass.length() < 6)   { showError("Password must be at least 6 characters."); return; }
        if (!pass.equals(confirm)) { showError("Passwords do not match."); return; }
        if (userService.usernameExists(username)) { showError("Username already taken."); return; }
        if (userService.emailExists(email))       { showError("Email already registered."); return; }

        User user = userService.register(username, pass, name, email);
        if (user != null) openDashboard(user);
    }

    private void openDashboard(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(user, userService));
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void addDragSupport(JPanel panel) {
        final Point[] start = {null};
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { start[0] = e.getPoint(); }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (start[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - start[0].x, loc.y + e.getY() - start[0].y);
                }
            }
        });
    }
}