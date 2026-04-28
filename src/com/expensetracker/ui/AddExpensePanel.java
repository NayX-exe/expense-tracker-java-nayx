package com.expensetracker.ui;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AddExpensePanel extends JPanel {

    private final User user;
    private final ExpenseService expenseService;
    private final MainFrame mainFrame;

    private JTextField titleField, amountField, dateField, noteField;
    private JComboBox<String> categoryCombo;
    private JButton expenseTab, incomeTab;
    private String currentType = "expense";

    public AddExpensePanel(User user, ExpenseService expenseService, MainFrame mainFrame) {
        this.user = user;
        this.expenseService = expenseService;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppConfig.BG_DARK);
        build();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(UIHelper.label("Add Transaction", 22, true));
        left.add(UIHelper.labelSecondary("Record a new income or expense", 13));
        header.add(left, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(AppConfig.BG_DARK);
        center.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridx = 0; gbc.gridy = 0;
        JPanel formCard = buildFormCard();
        formCard.setPreferredSize(new Dimension(520, 580));
        center.add(formCard, gbc);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildFormCard() {
        JPanel card = UIHelper.card(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.weightx = 1; g.gridx = 0; g.gridy = 0;

        // IMPORTANT: categoryCombo must be created BEFORE buildTypeToggle()
        categoryCombo = UIHelper.styledCombo(AppConfig.EXPENSE_CATEGORIES);

        JPanel typeToggle = buildTypeToggle();
        typeToggle.setPreferredSize(new Dimension(0, 46));
        card.add(typeToggle, g); g.gridy++;

        g.insets = new Insets(16, 0, 4, 0);
        card.add(UIHelper.labelSecondary("Amount", 12), g); g.gridy++;

        g.insets = new Insets(0, 0, 8, 0);
        amountField = new JTextField() {
            @Override protected void paintComponent(Graphics g2) {
                super.paintComponent(g2);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D gg = (Graphics2D) g2;
                    gg.setColor(AppConfig.TEXT_SECONDARY);
                    gg.setFont(AppConfig.fontPlain(22));
                    gg.drawString("0.00", 12, getHeight() / 2 + 8);
                }
            }
        };
        amountField.setFont(AppConfig.fontBold(28));
        amountField.setForeground(AppConfig.ACCENT_TEAL);
        amountField.setBackground(new Color(0x1E, 0x2D, 0x3D));
        amountField.setCaretColor(AppConfig.ACCENT_TEAL);
        amountField.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundBorder(10, AppConfig.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        amountField.setPreferredSize(new Dimension(0, 60));
        card.add(amountField, g); g.gridy++;

        g.insets = new Insets(4, 0, 4, 0);
        card.add(UIHelper.labelSecondary("Title", 12), g); g.gridy++;
        titleField = UIHelper.styledField("What did you spend on?");
        card.add(titleField, g); g.gridy++;

        card.add(UIHelper.labelSecondary("Category", 12), g); g.gridy++;
        card.add(categoryCombo, g); g.gridy++;

        card.add(UIHelper.labelSecondary("Date", 12), g); g.gridy++;
        dateField = UIHelper.styledField("yyyy-MM-dd");
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        card.add(dateField, g); g.gridy++;

        card.add(UIHelper.labelSecondary("Note (optional)", 12), g); g.gridy++;
        noteField = UIHelper.styledField("Add a note...");
        card.add(noteField, g); g.gridy++;

        g.insets = new Insets(20, 0, 0, 0);
        JButton submitBtn = UIHelper.primaryButton("Add Transaction ->");
        submitBtn.setPreferredSize(new Dimension(0, 50));
        submitBtn.addActionListener(e -> submit());
        card.add(submitBtn, g);

        return card;
    }

    private JPanel buildTypeToggle() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 4, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x12, 0x1E, 0x2C));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        expenseTab = buildToggleBtn("Expense", true);
        incomeTab  = buildToggleBtn("Income", false);
        expenseTab.addActionListener(e -> selectType("expense"));
        incomeTab.addActionListener(e -> selectType("income"));
        panel.add(expenseTab);
        panel.add(incomeTab);
        selectType("expense"); // safe now since categoryCombo already exists
        return panel;
    }

    private JButton buildToggleBtn(String text, boolean isExpense) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                boolean active = (isExpense && "expense".equals(currentType))
                              || (!isExpense && "income".equals(currentType));
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    Color c = isExpense ? AppConfig.EXPENSE_RED : AppConfig.INCOME_GREEN;
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(c);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth()-1.5f, getHeight()-1.5f, 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(AppConfig.fontBold(14));
        b.setForeground(isExpense ? AppConfig.EXPENSE_RED : AppConfig.INCOME_GREEN);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void selectType(String type) {
        currentType = type;
        String[] cats = "expense".equals(type) ? AppConfig.EXPENSE_CATEGORIES : AppConfig.INCOME_CATEGORIES;
        categoryCombo.setModel(new DefaultComboBoxModel<>(cats));
        if (expenseTab != null) expenseTab.repaint();
        if (incomeTab  != null) incomeTab.repaint();
    }

    private void submit() {
        String title  = titleField.getText().trim();
        String amtStr = amountField.getText().trim();
        String date   = dateField.getText().trim();
        String cat    = (String) categoryCombo.getSelectedItem();
        String note   = noteField.getText().trim();

        if (title.isEmpty())  { showError("Please enter a title.");  return; }
        if (amtStr.isEmpty()) { showError("Please enter an amount."); return; }
        double amount;
        try { amount = Double.parseDouble(amtStr); }
        catch (NumberFormatException e) { showError("Invalid amount."); return; }
        if (amount <= 0) { showError("Amount must be positive."); return; }
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showError("Date must be yyyy-MM-dd format."); return;
        }

        Expense e = new Expense(UUID.randomUUID().toString(), user.getUsername(),
                title, amount, cat, date, note, currentType, null);
        expenseService.add(e);
        JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
        reset();
    }

    public void reset() {
        titleField.setText("");
        amountField.setText("");
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        noteField.setText("");
        selectType("expense");
        categoryCombo.setSelectedIndex(0);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}