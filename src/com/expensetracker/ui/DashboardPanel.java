package com.expensetracker.ui;

import com.expensetracker.model.Expense;
import com.expensetracker.service.*;
import com.expensetracker.model.User;
import com.expensetracker.util.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final User user;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final MainFrame mainFrame;

    private JPanel statsRow, recentList, chartPanel;

    public DashboardPanel(User user, ExpenseService expenseService,
                          BudgetService budgetService, MainFrame mainFrame) {
        this.user = user;
        this.expenseService = expenseService;
        this.budgetService = budgetService;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppConfig.BG_DARK);
        build();
    }

    private void build() {
        // Top header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));

        JPanel headLeft = new JPanel(new GridLayout(2, 1));
        headLeft.setOpaque(false);
        String greet = getGreeting();
        JLabel greetLabel = UIHelper.label(greet + ", " + user.getFullName().split(" ")[0] + "!", 22, true);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
        JLabel dateLabel = UIHelper.labelSecondary(today, 13);
        headLeft.add(greetLabel); headLeft.add(dateLabel);

        JButton addBtn = UIHelper.primaryButton("+ Add Transaction");
        addBtn.addActionListener(e -> mainFrame.showPanel("Add New"));

        header.add(headLeft, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Scroll content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppConfig.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

        statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        content.add(statsRow); content.add(Box.createVerticalStrut(20));

        // Middle row: chart + recent
        JPanel midRow = new JPanel(new GridLayout(1, 2, 20, 0));
        midRow.setOpaque(false);
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        chartPanel = buildChartSection();
        midRow.add(chartPanel);

        JPanel recentSection = buildRecentSection();
        midRow.add(recentSection);

        content.add(midRow); content.add(Box.createVerticalStrut(20));

        // Category breakdown
        JPanel catSection = buildCategorySection();
        catSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        content.add(catSection);

        add(UIHelper.darkScroll(content), BorderLayout.CENTER);
    }

    public void refresh() {
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Expense> monthly = expenseService.getByUserAndMonth(user.getUsername(), month);
        List<Expense> all = expenseService.getByUser(user.getUsername());

        double income  = expenseService.totalIncome(monthly);
        double expense = expenseService.totalExpenses(monthly);
        double balance = income - expense;
        double savings = income > 0 ? (balance / income) * 100 : 0;

        statsRow.removeAll();
        statsRow.add(buildStatCard("This Month Income", income, AppConfig.INCOME_GREEN, "↑", false));
        statsRow.add(buildStatCard("This Month Expenses", expense, AppConfig.EXPENSE_RED, "↓", false));
        statsRow.add(buildStatCard("Balance", balance, balance >= 0 ? AppConfig.ACCENT_TEAL : AppConfig.DANGER, "⊘", false));
        statsRow.add(buildStatCard("Savings Rate", savings, AppConfig.ACCENT_GOLD, "%", true));
        statsRow.revalidate(); statsRow.repaint();

        // Refresh chart
        refreshChart();

        // Refresh recent
        SwingUtilities.invokeLater(this::refreshRecent);
    }

    private JPanel buildStatCard(String title, double value, Color accent, String icon, boolean isPercent) {
        JPanel card = UIHelper.card(new BorderLayout(0, 8));

        // Top: icon
        JLabel iconL = new JLabel(icon);
        iconL.setFont(AppConfig.fontBold(18));
        iconL.setForeground(accent);
        card.add(iconL, BorderLayout.NORTH);

        // Middle: value
        String valStr = isPercent ? String.format("%.1f%%", value) : String.format("$%.2f", value);
        JLabel valLabel = UIHelper.label(valStr, 22, true);
        valLabel.setForeground(accent);
        card.add(valLabel, BorderLayout.CENTER);

        // Bottom: title
        JLabel titleL = UIHelper.labelSecondary(title, 11);
        card.add(titleL, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildChartSection() {
        JPanel wrap = UIHelper.card(new BorderLayout());
        JLabel title = UIHelper.label("Monthly Overview", 14, true);
        wrap.add(title, BorderLayout.NORTH);

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g, this);
            }
        };
        chart.setOpaque(false);
        wrap.add(chart, BorderLayout.CENTER);
        wrap.putClientProperty("chartComp", chart);
        return wrap;
    }

    private void refreshChart() {
        // Find chart component and repaint
        Component chart = chartPanel.getClientProperty("chartComp") instanceof Component ?
                (Component) chartPanel.getClientProperty("chartComp") : null;
        if (chart != null) chart.repaint();
        chartPanel.repaint();
    }

    private void drawBarChart(Graphics g, Component comp) {
        String year = String.valueOf(LocalDate.now().getYear());
        Map<String, double[]> monthly = expenseService.monthlyTotals(user.getUsername(), year);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = comp.getWidth(), h = comp.getHeight();
        int padL = 10, padR = 10, padT = 20, padB = 30;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;

        double maxVal = 1;
        for (double[] v : monthly.values()) maxVal = Math.max(maxVal, Math.max(v[0], v[1]));

        String[] months = {"J","F","M","A","M","J","J","A","S","O","N","D"};
        int count = 12;
        float barGroupW = (float) chartW / count;
        float barW = barGroupW * 0.35f;
        int currentMonth = LocalDate.now().getMonthValue();

        int idx = 0;
        for (Map.Entry<String, double[]> entry : monthly.entrySet()) {
            double[] vals = entry.getValue();
            float x = padL + idx * barGroupW;

            // Income bar
            float incH = (float)(vals[0] / maxVal * chartH);
            g2.setColor(new Color(AppConfig.INCOME_GREEN.getRed(),
                    AppConfig.INCOME_GREEN.getGreen(), AppConfig.INCOME_GREEN.getBlue(), 180));
            g2.fill(new RoundRectangle2D.Float(x + 2, padT + chartH - incH, barW, incH, 4, 4));

            // Expense bar
            float expH = (float)(vals[1] / maxVal * chartH);
            g2.setColor(new Color(AppConfig.EXPENSE_RED.getRed(),
                    AppConfig.EXPENSE_RED.getGreen(), AppConfig.EXPENSE_RED.getBlue(), 180));
            g2.fill(new RoundRectangle2D.Float(x + barW + 4, padT + chartH - expH, barW, expH, 4, 4));

            // Month label
            g2.setColor(idx + 1 == currentMonth ? AppConfig.ACCENT_TEAL : AppConfig.TEXT_SECONDARY);
            g2.setFont(AppConfig.fontPlain(10));
            g2.drawString(months[idx], x + barGroupW / 2 - 4, h - 8);
            idx++;
        }

        // Legend
        g2.setColor(AppConfig.INCOME_GREEN);
        g2.fillRect(w - 120, 4, 10, 10);
        g2.setColor(AppConfig.TEXT_SECONDARY);
        g2.setFont(AppConfig.fontPlain(10));
        g2.drawString("Income", w - 106, 13);
        g2.setColor(AppConfig.EXPENSE_RED);
        g2.fillRect(w - 55, 4, 10, 10);
        g2.drawString("Exp", w - 41, 13);

        g2.dispose();
    }

    private JPanel buildRecentSection() {
        JPanel wrap = UIHelper.card(new BorderLayout());
        JLabel title = UIHelper.label("Recent Transactions", 14, true);
        JButton viewAll = UIHelper.ghostButton("View all →");
        viewAll.addActionListener(e -> mainFrame.showPanel("Transactions"));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        top.add(viewAll, BorderLayout.EAST);
        wrap.add(top, BorderLayout.NORTH);
        wrap.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

        recentList = new JPanel();
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setOpaque(false);
        wrap.add(UIHelper.darkScroll(recentList), BorderLayout.CENTER);
        return wrap;
    }

    private void refreshRecent() {
        recentList.removeAll();
        List<Expense> list = expenseService.getByUser(user.getUsername());
        int show = Math.min(5, list.size());
        if (show == 0) {
            JLabel empty = UIHelper.labelSecondary("No transactions yet", 13);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            recentList.add(Box.createVerticalStrut(20));
            recentList.add(empty);
        }
        for (int i = 0; i < show; i++) {
            recentList.add(buildRecentRow(list.get(i)));
            if (i < show - 1) recentList.add(Box.createVerticalStrut(4));
        }
        recentList.revalidate(); recentList.repaint();
    }

    private JPanel buildRecentRow(Expense e) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        // Category dot
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.getCategoryColor(e.getCategory()));
                g2.fillOval(3, 3, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                String em = categoryEmoji(e.getCategory());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(em, (34 - fm.stringWidth(em)) / 2, (34 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(34, 34));
        dot.setOpaque(false);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel titleL = UIHelper.label(e.getTitle(), 13, false);
        JLabel catL = UIHelper.labelSecondary(e.getCategory() + " · " + e.getDate(), 11);
        info.add(titleL); info.add(catL);

        boolean isIncome = "income".equals(e.getType());
        JLabel amt = UIHelper.amountLabel(e.getAmount(), isIncome);
        amt.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(dot, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(amt, BorderLayout.EAST);
        return row;
    }

    private JPanel buildCategorySection() {
        JPanel wrap = UIHelper.card(new BorderLayout());
        JLabel title = UIHelper.label("Spending by Category (This Month)", 14, true);
        wrap.add(title, BorderLayout.NORTH);
        wrap.add(Box.createVerticalStrut(10));

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Expense> monthly = expenseService.getByUserAndMonth(user.getUsername(), month);
        Map<String, Double> catMap = expenseService.expenseByCategory(monthly);
        double total = expenseService.totalExpenses(monthly);

        JPanel bars = new JPanel(new GridLayout(0, 1, 0, 8));
        bars.setOpaque(false);
        bars.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        if (catMap.isEmpty()) {
            bars.add(UIHelper.labelSecondary("No expense data for this month", 13));
        } else {
            int shown = 0;
            for (Map.Entry<String, Double> e : catMap.entrySet()) {
                if (shown++ >= 5) break;
                bars.add(buildCategoryBar(e.getKey(), e.getValue(), total));
            }
        }
        wrap.add(bars, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildCategoryBar(String cat, double amount, double total) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel catL = UIHelper.label(cat, 12, false);
        catL.setPreferredSize(new Dimension(130, 20));

        double pct = total > 0 ? (amount / total) * 100 : 0;
        JPanel barContainer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.BORDER_COLOR);
                g2.fill(new RoundRectangle2D.Float(0, 5, getWidth(), 8, 8, 8));
                Color c = AppConfig.getCategoryColor(cat);
                int barW = (int)(getWidth() * pct / 100);
                if (barW > 0) {
                    g2.setColor(c);
                    g2.fill(new RoundRectangle2D.Float(0, 5, barW, 8, 8, 8));
                }
                g2.dispose();
            }
        };
        barContainer.setOpaque(false);
        barContainer.setPreferredSize(new Dimension(0, 18));

        JLabel pctL = UIHelper.labelSecondary(String.format("$%.0f (%.0f%%)", amount, pct), 11);
        pctL.setHorizontalAlignment(SwingConstants.RIGHT);
        pctL.setPreferredSize(new Dimension(120, 20));

        row.add(catL, BorderLayout.WEST);
        row.add(barContainer, BorderLayout.CENTER);
        row.add(pctL, BorderLayout.EAST);
        return row;
    }

    private String getGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    private String categoryEmoji(String cat) {
        switch (cat) {
            case "Food & Dining": return "🍔";
            case "Transport": return "🚗";
            case "Shopping": return "🛍";
            case "Entertainment": return "🎬";
            case "Health": return "💊";
            case "Education": return "📚";
            case "Housing": return "🏠";
            case "Utilities": return "⚡";
            case "Salary": return "💵";
            case "Freelance": return "💻";
            case "Investment": return "📈";
            default: return "💸";
        }
    }
}