package com.expensetracker.ui;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class MonthlyPanel extends JPanel {

    private final User user;
    private final ExpenseService expenseService;

    private JPanel monthsGrid;
    private JPanel detailPanel;
    private String selectedMonth;

    public MonthlyPanel(User user, ExpenseService expenseService) {
        this.user = user;
        this.expenseService = expenseService;
        this.selectedMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        setLayout(new BorderLayout());
        setBackground(AppConfig.BG_DARK);
        build();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));
        header.add(UIHelper.label("Monthly Overview", 22, true), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Split: left = month cards, right = detail
        JPanel split = new JPanel(new BorderLayout(20, 0));
        split.setBackground(AppConfig.BG_DARK);
        split.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

        // Left: year selector + month grid
        JPanel leftPanel = new JPanel(new BorderLayout(0, 16));
        leftPanel.setBackground(AppConfig.BG_DARK);
        leftPanel.setPreferredSize(new Dimension(320, 0));

        // Year selector
        int curYear = LocalDate.now().getYear();
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) years[i] = String.valueOf(curYear - i);
        JComboBox<String> yearCombo = UIHelper.styledCombo(years);
        yearCombo.addActionListener(e -> buildMonthGrid((String) yearCombo.getSelectedItem()));
        leftPanel.add(yearCombo, BorderLayout.NORTH);

        monthsGrid = new JPanel(new GridLayout(4, 3, 10, 10));
        monthsGrid.setBackground(AppConfig.BG_DARK);
        leftPanel.add(monthsGrid, BorderLayout.CENTER);

        split.add(leftPanel, BorderLayout.WEST);

        // Right: detail
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(AppConfig.BG_DARK);
        split.add(detailPanel, BorderLayout.CENTER);

        add(split, BorderLayout.CENTER);
        buildMonthGrid(String.valueOf(curYear));
        showMonthDetail(selectedMonth);
    }

    private void buildMonthGrid(String year) {
        monthsGrid.removeAll();
        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (int m = 1; m <= 12; m++) {
            String key = year + "-" + String.format("%02d", m);
            List<Expense> expenses = expenseService.getByUserAndMonth(user.getUsername(), key);
            double inc = expenseService.totalIncome(expenses);
            double exp = expenseService.totalExpenses(expenses);
            boolean isCurrent = key.equals(currentMonth);
            boolean isSelected = key.equals(selectedMonth);

            final String monthKey = key;
            JPanel card = new JPanel(new GridLayout(4, 1)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = isSelected ? new Color(0x00, 0xC8, 0xB4, 40)
                             : isCurrent  ? new Color(0x7C, 0x3A, 0xFF, 20)
                                          : AppConfig.BG_CARD;
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    if (isSelected || isCurrent) {
                        g2.setColor(isSelected ? AppConfig.ACCENT_TEAL : AppConfig.ACCENT_PURPLE);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, getWidth()-1.5f, getHeight()-1.5f, 12, 12));
                    }
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel nameL = UIHelper.label(monthNames[m-1], 13, true);
            nameL.setForeground(isCurrent ? AppConfig.ACCENT_TEAL : AppConfig.TEXT_PRIMARY);

            JLabel yearL = UIHelper.labelSecondary(year, 10);

            JLabel incL = new JLabel(inc > 0 ? "+$" + String.format("%.0f", inc) : "—");
            incL.setFont(AppConfig.fontPlain(11));
            incL.setForeground(inc > 0 ? AppConfig.INCOME_GREEN : AppConfig.TEXT_SECONDARY);

            JLabel expL = new JLabel(exp > 0 ? "-$" + String.format("%.0f", exp) : "—");
            expL.setFont(AppConfig.fontPlain(11));
            expL.setForeground(exp > 0 ? AppConfig.EXPENSE_RED : AppConfig.TEXT_SECONDARY);

            card.add(nameL); card.add(yearL); card.add(incL); card.add(expL);

            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectedMonth = monthKey;
                    buildMonthGrid(year);
                    showMonthDetail(monthKey);
                }
            });

            monthsGrid.add(card);
        }
        monthsGrid.revalidate(); monthsGrid.repaint();
    }

    private void showMonthDetail(String yearMonth) {
        detailPanel.removeAll();
        List<Expense> list = expenseService.getByUserAndMonth(user.getUsername(), yearMonth);
        double income  = expenseService.totalIncome(list);
        double expense = expenseService.totalExpenses(list);
        double balance = income - expense;

        // Parse year-month for display
        String[] parts = yearMonth.split("-");
        String[] monthNames = {"","January","February","March","April","May","June",
                "July","August","September","October","November","December"};
        String display = monthNames[Integer.parseInt(parts[1])] + " " + parts[0];

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppConfig.BG_DARK);

        // Month heading
        JLabel heading = UIHelper.label(display, 18, true);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading); content.add(Box.createVerticalStrut(16));

        // Summary cards
        JPanel summary = new JPanel(new GridLayout(1, 3, 12, 0));
        summary.setOpaque(false);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        summary.add(buildMiniCard("Income", income, AppConfig.INCOME_GREEN));
        summary.add(buildMiniCard("Expenses", expense, AppConfig.EXPENSE_RED));
        summary.add(buildMiniCard("Balance", balance, balance >= 0 ? AppConfig.ACCENT_TEAL : AppConfig.DANGER));
        content.add(summary); content.add(Box.createVerticalStrut(20));

        // Pie chart
        JPanel pieWrap = UIHelper.card(new BorderLayout());
        pieWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        pieWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        pieWrap.add(UIHelper.label("Category Breakdown", 13, true), BorderLayout.NORTH);

        Map<String, Double> catMap = expenseService.expenseByCategory(list);
        JPanel pieComp = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPie(g, this, catMap, expense);
            }
        };
        pieComp.setOpaque(false);
        pieWrap.add(pieComp, BorderLayout.CENTER);
        content.add(pieWrap); content.add(Box.createVerticalStrut(20));

        // Transaction list
        JPanel txWrap = UIHelper.card(new BorderLayout());
        txWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        txWrap.add(UIHelper.label("Transactions (" + list.size() + ")", 13, true), BorderLayout.NORTH);

        JPanel txList = new JPanel(new GridLayout(0, 1, 0, 2));
        txList.setOpaque(false);
        for (Expense e : list) txList.add(buildTxRow(e));
        txWrap.add(UIHelper.darkScroll(txList), BorderLayout.CENTER);
        content.add(txWrap);

        detailPanel.add(UIHelper.darkScroll(content), BorderLayout.CENTER);
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void drawPie(Graphics g, Component comp, Map<String, Double> catMap, double total) {
        if (catMap.isEmpty() || total == 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(AppConfig.TEXT_SECONDARY);
            g2.setFont(AppConfig.fontPlain(12));
            g2.drawString("No expense data", comp.getWidth()/2 - 50, comp.getHeight()/2);
            g2.dispose();
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(comp.getWidth() / 2, comp.getHeight() - 20);
        int x = 20, y = 10;
        double startAngle = 0;
        List<Map.Entry<String, Double>> entries = new ArrayList<>(catMap.entrySet());

        for (Map.Entry<String, Double> entry : entries) {
            double pct = entry.getValue() / total;
            double arc = pct * 360;
            g2.setColor(AppConfig.getCategoryColor(entry.getKey()));
            g2.fill(new Arc2D.Double(x, y, size, size, startAngle, arc, Arc2D.PIE));
            startAngle += arc;
        }

        // Hole (donut)
        int hole = size / 3;
        g2.setColor(AppConfig.BG_CARD);
        g2.fillOval(x + hole, y + hole, size - hole*2, size - hole*2);

        // Legend
        int lx = x + size + 20, ly = y + 10;
        g2.setFont(AppConfig.fontPlain(11));
        for (Map.Entry<String, Double> entry : entries) {
            if (ly > comp.getHeight() - 10) break;
            g2.setColor(AppConfig.getCategoryColor(entry.getKey()));
            g2.fillRoundRect(lx, ly, 10, 10, 4, 4);
            g2.setColor(AppConfig.TEXT_SECONDARY);
            String label = entry.getKey() + " $" + String.format("%.0f", entry.getValue());
            g2.drawString(label, lx + 14, ly + 10);
            ly += 20;
        }
        g2.dispose();
    }

    private JPanel buildMiniCard(String title, double val, Color color) {
        JPanel card = UIHelper.card(new GridLayout(2, 1));
        JLabel valL = UIHelper.label(String.format("$%.2f", val), 16, true);
        valL.setForeground(color);
        card.add(valL);
        card.add(UIHelper.labelSecondary(title, 11));
        return card;
    }

    private JPanel buildTxRow(Expense e) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(UIHelper.label(e.getTitle(), 13, false));
        left.add(UIHelper.labelSecondary(e.getCategory() + " · " + e.getDate(), 11));

        boolean isIncome = "income".equals(e.getType());
        JLabel amt = UIHelper.amountLabel(e.getAmount(), isIncome);
        amt.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(left, BorderLayout.CENTER);
        row.add(amt, BorderLayout.EAST);
        return row;
    }

    public void refresh() {
        buildMonthGrid(selectedMonth.substring(0, 4));
        showMonthDetail(selectedMonth);
    }
}