package com.expensetracker.ui;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionsPanel extends JPanel {

    private final User user;
    private final ExpenseService expenseService;
    private final MainFrame mainFrame;

    private JTextField searchField;
    private JComboBox<String> typeFilter, categoryFilter, monthFilter;
    private JPanel listContainer;
    private JLabel totalLabel;

    public TransactionsPanel(User user, ExpenseService expenseService, MainFrame mainFrame) {
        this.user = user;
        this.expenseService = expenseService;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppConfig.BG_DARK);
        build();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(28, 28, 16, 28));
        JPanel headLeft = new JPanel(new GridLayout(2, 1));
        headLeft.setOpaque(false);
        headLeft.add(UIHelper.label("All Transactions", 22, true));
        totalLabel = UIHelper.labelSecondary("Loading...", 13);
        headLeft.add(totalLabel);
        header.add(headLeft, BorderLayout.WEST);

        JButton addBtn = UIHelper.primaryButton("+ Add Transaction");
        addBtn.addActionListener(e -> mainFrame.showPanel("Add New"));
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Filters
        JPanel filterBar = buildFilterBar();
        add(filterBar, BorderLayout.CENTER);
    }

    private JPanel buildFilterBar() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(AppConfig.BG_DARK);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));

        // Filter row
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setBackground(AppConfig.BG_DARK);
        filters.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        searchField = UIHelper.styledField("Search transactions...");
        searchField.setPreferredSize(new Dimension(220, 38));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        typeFilter = UIHelper.styledCombo(new String[]{"All Types", "Expense", "Income"});
        typeFilter.setPreferredSize(new Dimension(130, 38));
        typeFilter.addActionListener(e -> applyFilters());

        String[] cats = new String[AppConfig.ALL_CATEGORIES.length + 1];
        cats[0] = "All Categories";
        System.arraycopy(AppConfig.ALL_CATEGORIES, 0, cats, 1, AppConfig.ALL_CATEGORIES.length);
        categoryFilter = UIHelper.styledCombo(cats);
        categoryFilter.setPreferredSize(new Dimension(160, 38));
        categoryFilter.addActionListener(e -> applyFilters());

        // Build month filter options
        List<String> months = expenseService.getMonthsWithData(user.getUsername());
        String[] monthOpts = new String[months.size() + 1];
        monthOpts[0] = "All Months";
        for (int i = 0; i < months.size(); i++) monthOpts[i+1] = months.get(i);
        monthFilter = UIHelper.styledCombo(monthOpts);
        monthFilter.setPreferredSize(new Dimension(140, 38));
        monthFilter.addActionListener(e -> applyFilters());

        JButton clearBtn = UIHelper.ghostButton("Clear filters");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedIndex(0);
            categoryFilter.setSelectedIndex(0);
            monthFilter.setSelectedIndex(0);
        });

        filters.add(searchField); filters.add(typeFilter);
        filters.add(categoryFilter); filters.add(monthFilter);
        filters.add(clearBtn);
        wrap.add(filters, BorderLayout.NORTH);

        // List area
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(AppConfig.BG_DARK);

        wrap.add(UIHelper.darkScroll(listContainer), BorderLayout.CENTER);
        return wrap;
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase().trim();
        String type   = (String) typeFilter.getSelectedItem();
        String cat    = (String) categoryFilter.getSelectedItem();
        String month  = (String) monthFilter.getSelectedItem();

        List<Expense> all = expenseService.getByUser(user.getUsername());
        List<Expense> filtered = all.stream().filter(e -> {
            if (!search.isEmpty() && !e.getTitle().toLowerCase().contains(search)
                    && !e.getCategory().toLowerCase().contains(search)
                    && !(e.getNote() != null && e.getNote().toLowerCase().contains(search)))
                return false;
            if (!"All Types".equals(type)) {
                String t = type.equalsIgnoreCase("Expense") ? "expense" : "income";
                if (!e.getType().equals(t)) return false;
            }
            if (!"All Categories".equals(cat) && !e.getCategory().equals(cat)) return false;
            if (!"All Months".equals(month) && !e.getDate().startsWith(month)) return false;
            return true;
        }).collect(Collectors.toList());

        renderList(filtered);
    }

    private void renderList(List<Expense> list) {
        listContainer.removeAll();

        double totalInc = expenseService.totalIncome(list);
        double totalExp = expenseService.totalExpenses(list);
        totalLabel.setText(list.size() + " transactions · Income: $" +
                String.format("%.2f", totalInc) + " · Expenses: $" + String.format("%.2f", totalExp));

        if (list.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 0));
            JLabel emptyL = UIHelper.labelSecondary("No transactions found", 16);
            emptyL.setHorizontalAlignment(SwingConstants.CENTER);
            empty.add(emptyL, BorderLayout.CENTER);
            listContainer.add(empty);
        }

        // Group by date
        Map<String, List<Expense>> byDate = new LinkedHashMap<>();
        for (Expense e : list) byDate.computeIfAbsent(e.getDate(), k -> new ArrayList<>()).add(e);

        for (Map.Entry<String, List<Expense>> entry : byDate.entrySet()) {
            // Date header
            JPanel dateHeader = new JPanel(new BorderLayout(8, 0));
            dateHeader.setOpaque(false);
            dateHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            dateHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
            dateHeader.add(UIHelper.labelSecondary(entry.getKey(), 11), BorderLayout.WEST);
            dateHeader.add(UIHelper.separator(), BorderLayout.CENTER);
            listContainer.add(dateHeader);

            for (Expense e : entry.getValue()) {
                listContainer.add(buildRow(e));
                listContainer.add(Box.createVerticalStrut(2));
            }
        }

        listContainer.revalidate(); listContainer.repaint();
    }

    private JPanel buildRow(Expense e) {
        JPanel row = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Left color indicator
        JPanel colorDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.getCategoryColor(e.getCategory()));
                g2.fillOval(2, 2, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                String em = categoryEmoji(e.getCategory());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(em, (40 - fm.stringWidth(em)) / 2, (40 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        colorDot.setPreferredSize(new Dimension(40, 40));
        colorDot.setOpaque(false);

        // Info
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel titleL = UIHelper.label(e.getTitle(), 14, false);
        String noteStr = (e.getNote() != null && !e.getNote().isEmpty()) ? " · " + e.getNote() : "";
        JLabel metaL = UIHelper.labelSecondary(e.getCategory() + noteStr, 11);
        info.add(titleL); info.add(metaL);

        // Right: amount + actions
        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);
        boolean isIncome = "income".equals(e.getType());
        JLabel amt = UIHelper.amountLabel(e.getAmount(), isIncome);
        amt.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);

        JButton editBtn = new JButton("✎");
        styleIconBtn(editBtn, AppConfig.ACCENT_TEAL);
        editBtn.addActionListener(ev -> openEditDialog(e));

        JButton delBtn = new JButton("✕");
        styleIconBtn(delBtn, AppConfig.DANGER);
        delBtn.addActionListener(ev -> deleteExpense(e));

        actions.add(editBtn); actions.add(delBtn);

        right.add(amt, BorderLayout.NORTH);
        right.add(actions, BorderLayout.SOUTH);

        row.add(colorDot, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void styleIconBtn(JButton b, Color color) {
        b.setFont(AppConfig.fontBold(11));
        b.setForeground(color);
        b.setPreferredSize(new Dimension(28, 28));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void openEditDialog(Expense e) {
        JDialog dlg = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Transaction", true);
        dlg.setSize(420, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setBackground(AppConfig.BG_CARD);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppConfig.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1; gbc.gridx = 0; gbc.gridy = 0;

        panel.add(UIHelper.label("Edit Transaction", 18, true), gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Title", 12), gbc); gbc.gridy++;
        JTextField titleF = UIHelper.styledField("Title");
        titleF.setText(e.getTitle());
        panel.add(titleF, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Amount", 12), gbc); gbc.gridy++;
        JTextField amtF = UIHelper.styledField("Amount");
        amtF.setText(String.valueOf(e.getAmount()));
        panel.add(amtF, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Type", 12), gbc); gbc.gridy++;
        JComboBox<String> typeC = UIHelper.styledCombo(new String[]{"expense", "income"});
        typeC.setSelectedItem(e.getType());
        panel.add(typeC, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Category", 12), gbc); gbc.gridy++;
        JComboBox<String> catC = UIHelper.styledCombo(AppConfig.ALL_CATEGORIES);
        catC.setSelectedItem(e.getCategory());
        panel.add(catC, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Date (yyyy-MM-dd)", 12), gbc); gbc.gridy++;
        JTextField dateF = UIHelper.styledField("yyyy-MM-dd");
        dateF.setText(e.getDate());
        panel.add(dateF, gbc); gbc.gridy++;

        panel.add(UIHelper.labelSecondary("Note", 12), gbc); gbc.gridy++;
        JTextField noteF = UIHelper.styledField("Optional note");
        noteF.setText(e.getNote() != null ? e.getNote() : "");
        panel.add(noteF, gbc); gbc.gridy++;

        gbc.insets = new Insets(16, 0, 0, 0);
        JButton saveBtn = UIHelper.primaryButton("Save Changes");
        saveBtn.addActionListener(ev -> {
            try {
                e.setTitle(titleF.getText().trim());
                e.setAmount(Double.parseDouble(amtF.getText().trim()));
                e.setType((String) typeC.getSelectedItem());
                e.setCategory((String) catC.getSelectedItem());
                e.setDate(dateF.getText().trim());
                e.setNote(noteF.getText().trim());
                expenseService.update(e);
                dlg.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveBtn, gbc);

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private void deleteExpense(Expense e) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + e.getTitle() + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            expenseService.delete(e.getId());
            refresh();
        }
    }

    public void refresh() {
        // Rebuild month filter
        List<String> months = expenseService.getMonthsWithData(user.getUsername());
        String[] monthOpts = new String[months.size() + 1];
        monthOpts[0] = "All Months";
        for (int i = 0; i < months.size(); i++) monthOpts[i+1] = months.get(i);
        monthFilter.setModel(new DefaultComboBoxModel<>(monthOpts));
        applyFilters();
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