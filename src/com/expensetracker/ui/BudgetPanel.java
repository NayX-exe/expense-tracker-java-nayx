package com.expensetracker.ui;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class BudgetPanel extends JPanel {

    private final User user;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    private JComboBox<String> monthCombo;
    private JPanel budgetList;

    public BudgetPanel(User user, ExpenseService expenseService, BudgetService budgetService) {
        this.user = user;
        this.expenseService = expenseService;
        this.budgetService = budgetService;
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
        left.add(UIHelper.label("Budget Tracker", 22, true));
        left.add(UIHelper.labelSecondary("Set monthly spending limits per category", 13));
        header.add(left, BorderLayout.WEST);

        // Month selector
        String curMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String[] months = generateMonths();
        monthCombo = UIHelper.styledCombo(months);
        monthCombo.setSelectedItem(curMonth);
        monthCombo.setPreferredSize(new Dimension(140, 38));
        monthCombo.addActionListener(e -> refresh());
        header.add(monthCombo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setBackground(AppConfig.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

        budgetList = new JPanel();
        budgetList.setLayout(new BoxLayout(budgetList, BoxLayout.Y_AXIS));
        budgetList.setBackground(AppConfig.BG_DARK);

        content.add(UIHelper.darkScroll(budgetList), BorderLayout.CENTER);

        // Add budget form on right
        JPanel formPanel = buildSetBudgetForm();
        formPanel.setPreferredSize(new Dimension(280, 0));
        content.add(formPanel, BorderLayout.EAST);

        add(content, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildSetBudgetForm() {
        JPanel wrap = UIHelper.card(new BorderLayout(0, 12));
        wrap.add(UIHelper.label("Set Budget", 15, true), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1; gbc.gridx = 0; gbc.gridy = 0;

        form.add(UIHelper.labelSecondary("Category", 11), gbc); gbc.gridy++;
        JComboBox<String> catC = UIHelper.styledCombo(AppConfig.EXPENSE_CATEGORIES);
        form.add(catC, gbc); gbc.gridy++;

        form.add(UIHelper.labelSecondary("Monthly Limit ($)", 11), gbc); gbc.gridy++;
        JTextField limitF = UIHelper.styledField("e.g. 500");
        form.add(limitF, gbc); gbc.gridy++;

        gbc.insets = new Insets(14, 0, 0, 0);
        JButton saveBtn = UIHelper.primaryButton("Set Budget");
        saveBtn.addActionListener(e -> {
            try {
                String cat = (String) catC.getSelectedItem();
                double limit = Double.parseDouble(limitF.getText().trim());
                String month = (String) monthCombo.getSelectedItem();
                budgetService.set(new Budget(user.getUsername(), cat, limit, month));
                limitF.setText("");
                refresh();
                JOptionPane.showMessageDialog(this,
                    "Budget set for " + cat + ": $" + String.format("%.2f", limit), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        form.add(saveBtn, gbc);

        wrap.add(form, BorderLayout.CENTER);

        // Info note
        JLabel note = UIHelper.labelSecondary("Budgets are set per month and category.", 10);
        note.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrap.add(note, BorderLayout.SOUTH);

        return wrap;
    }

    public void refresh() {
        budgetList.removeAll();
        String month = (String) monthCombo.getSelectedItem();
        if (month == null) return;

        List<Expense> expenses = expenseService.getByUserAndMonth(user.getUsername(), month);
        Map<String, Double> spent = expenseService.expenseByCategory(expenses);
        List<Budget> budgets = budgetService.getByUserAndMonth(user.getUsername(), month);

        // Parse year-month
        String[] parts = month.split("-");
        String[] monthNames = {"","January","February","March","April","May","June",
                "July","August","September","October","November","December"};
        String display = monthNames[Integer.parseInt(parts[1])] + " " + parts[0];

        JLabel monthHead = UIHelper.label(display + " Budgets", 16, true);
        monthHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        monthHead.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        budgetList.add(monthHead);
        budgetList.add(Box.createVerticalStrut(16));

        if (budgets.isEmpty()) {
            JLabel empty = UIHelper.labelSecondary("No budgets set for this month. Use the form →", 13);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            budgetList.add(empty);
        }

        for (Budget b : budgets) {
            double spentAmt = spent.getOrDefault(b.getCategory(), 0.0);
            budgetList.add(buildBudgetCard(b, spentAmt));
            budgetList.add(Box.createVerticalStrut(12));
        }

        // Show unbudgeted categories that have spending
        budgetList.add(Box.createVerticalStrut(10));
        Set<String> budgetedCats = new HashSet<>();
        for (Budget b : budgets) budgetedCats.add(b.getCategory());

        boolean hasUnbudgeted = false;
        for (Map.Entry<String, Double> entry : spent.entrySet()) {
            if (!budgetedCats.contains(entry.getKey())) {
                if (!hasUnbudgeted) {
                    JLabel subHead = UIHelper.labelSecondary("Spending without budget:", 12);
                    subHead.setAlignmentX(Component.LEFT_ALIGNMENT);
                    budgetList.add(subHead);
                    budgetList.add(Box.createVerticalStrut(8));
                    hasUnbudgeted = true;
                }
                budgetList.add(buildUnbudgetedRow(entry.getKey(), entry.getValue()));
                budgetList.add(Box.createVerticalStrut(6));
            }
        }

        budgetList.revalidate(); budgetList.repaint();
    }

    private JPanel buildBudgetCard(Budget b, double spent) {
        JPanel card = UIHelper.card(new BorderLayout(0, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        double pct = b.getLimit() > 0 ? (spent / b.getLimit()) * 100 : 0;
        boolean overBudget = pct > 100;
        Color barColor = pct > 90 ? AppConfig.DANGER : pct > 70 ? AppConfig.ACCENT_GOLD : AppConfig.SUCCESS;

        // Top row
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        Color catColor = AppConfig.getCategoryColor(b.getCategory());
        JPanel catDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(catColor);
                g2.fillOval(0, 2, 10, 10);
                g2.dispose();
            }
        };
        catDot.setOpaque(false);
        catDot.setPreferredSize(new Dimension(14, 14));

        JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        catRow.setOpaque(false);
        catRow.add(catDot);
        catRow.add(UIHelper.label(b.getCategory(), 14, true));
        top.add(catRow, BorderLayout.WEST);

        JLabel pctL = UIHelper.label(String.format("%.0f%%", Math.min(pct, 999)), 13, true);
        pctL.setForeground(barColor);
        top.add(pctL, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Progress bar
        JPanel barBg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.BORDER_COLOR);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 10, 10, 10));
                int bw = (int) Math.min(getWidth(), getWidth() * Math.min(pct, 100) / 100);
                if (bw > 0) {
                    g2.setColor(barColor);
                    g2.fill(new RoundRectangle2D.Float(0, 0, bw, 10, 10, 10));
                }
                g2.dispose();
            }
        };
        barBg.setOpaque(false);
        barBg.setPreferredSize(new Dimension(0, 10));
        card.add(barBg, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel spentL = UIHelper.labelSecondary(String.format("Spent: $%.2f", spent), 11);
        JLabel limitL = UIHelper.labelSecondary(String.format("Limit: $%.2f", b.getLimit()), 11);
        JLabel remainL = new JLabel(overBudget ?
            String.format("Over by $%.2f!", spent - b.getLimit()) :
            String.format("$%.2f remaining", b.getLimit() - spent));
        remainL.setFont(AppConfig.fontPlain(11));
        remainL.setForeground(overBudget ? AppConfig.DANGER : AppConfig.SUCCESS);
        bottom.add(spentL, BorderLayout.WEST);
        bottom.add(remainL, BorderLayout.CENTER);
        bottom.add(limitL, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildUnbudgetedRow(String category, double spent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel catL = UIHelper.labelSecondary(category, 13);
        JLabel spentL = new JLabel("-$" + String.format("%.2f", spent));
        spentL.setFont(AppConfig.fontPlain(13));
        spentL.setForeground(AppConfig.EXPENSE_RED);
        JLabel noBudgetL = UIHelper.labelSecondary("No budget set", 11);
        noBudgetL.setHorizontalAlignment(SwingConstants.CENTER);

        row.add(catL, BorderLayout.WEST);
        row.add(noBudgetL, BorderLayout.CENTER);
        row.add(spentL, BorderLayout.EAST);
        return row;
    }

    private String[] generateMonths() {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 24; i++) {
            months.add(now.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        return months.toArray(new String[0]);
    }
}