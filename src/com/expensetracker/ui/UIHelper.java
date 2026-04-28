package com.expensetracker.ui;

import com.expensetracker.util.AppConfig;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UIHelper {

    // ─── Styled components ────────────────────────────────────────────────────

    public static JLabel label(String text, float size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(bold ? AppConfig.fontBold(size) : AppConfig.fontPlain(size));
        l.setForeground(AppConfig.TEXT_PRIMARY);
        return l;
    }

    public static JLabel labelSecondary(String text, float size) {
        JLabel l = new JLabel(text);
        l.setFont(AppConfig.fontPlain(size));
        l.setForeground(AppConfig.TEXT_SECONDARY);
        return l;
    }

    public static JTextField styledField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(AppConfig.TEXT_SECONDARY);
                    g2.setFont(AppConfig.fontPlain(13));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleField(tf);
        return tf;
    }

    public static JPasswordField styledPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(AppConfig.TEXT_SECONDARY);
                    g2.setFont(AppConfig.fontPlain(13));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleField(pf);
        return pf;
    }

    private static void styleField(JTextComponent tc) {
        tc.setBackground(new Color(0x1E, 0x2D, 0x3D));
        tc.setForeground(AppConfig.TEXT_PRIMARY);
        tc.setFont(AppConfig.fontPlain(14));
        tc.setCaretColor(AppConfig.ACCENT_TEAL);
        tc.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(8, AppConfig.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        tc.setOpaque(true);
        tc.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tc.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(8, AppConfig.ACCENT_TEAL, 1),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                tc.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(8, AppConfig.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }
        });
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(new Color(0x1E, 0x2D, 0x3D));
        cb.setForeground(AppConfig.TEXT_PRIMARY);
        cb.setFont(AppConfig.fontPlain(14));
        cb.setBorder(new RoundBorder(8, AppConfig.BORDER_COLOR, 1));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected, boolean cf) {
                super.getListCellRendererComponent(list, value, index, isSelected, cf);
                setBackground(isSelected ? AppConfig.ACCENT_TEAL.darker() : new Color(0x1A, 0x28, 0x38));
                setForeground(AppConfig.TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                return this;
            }
        });
        return cb;
    }

    public static JButton primaryButton(String text) {
        return styledButton(text, AppConfig.ACCENT_TEAL, AppConfig.BG_DARK);
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, AppConfig.DANGER, Color.WHITE);
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(AppConfig.fontBold(13));
        b.setForeground(AppConfig.ACCENT_TEAL);
        b.setBackground(new Color(0, 0, 0, 0));
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(AppConfig.ACCENT_TEAL.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(AppConfig.ACCENT_TEAL); }
        });
        return b;
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? bg.darker()
                           : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(AppConfig.fontBold(14));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        return b;
    }

    // ─── Card panel ───────────────────────────────────────────────────────────

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return p;
    }

    // ─── Separator ────────────────────────────────────────────────────────────

    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppConfig.BORDER_COLOR);
        sep.setBackground(AppConfig.BORDER_COLOR);
        return sep;
    }

    // ─── Amount label ─────────────────────────────────────────────────────────

    public static JLabel amountLabel(double amount, boolean isIncome) {
        String sign = isIncome ? "+" : "-";
        JLabel l = new JLabel(sign + String.format("$%.2f", amount));
        l.setFont(AppConfig.fontBold(14));
        l.setForeground(isIncome ? AppConfig.INCOME_GREEN : AppConfig.EXPENSE_RED);
        return l;
    }

    // ─── Scroll pane ──────────────────────────────────────────────────────────

    public static JScrollPane darkScroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(AppConfig.BG_DARK);
        sp.getViewport().setBackground(AppConfig.BG_DARK);
        sp.getVerticalScrollBar().setBackground(AppConfig.BG_DARK);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0x33, 0x4A, 0x60);
                trackColor = AppConfig.BG_DARK;
            }
            @Override protected JButton createDecreaseButton(int o) { return invisBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return invisBtn(); }
            private JButton invisBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
        return sp;
    }

    // ─── RoundBorder ─────────────────────────────────────────────────────────

    public static class RoundBorder extends AbstractBorder {
        private int radius, thick;
        private Color color;
        public RoundBorder(int radius, Color color, int thick) {
            this.radius = radius; this.color = color; this.thick = thick;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thick));
            g2.draw(new RoundRectangle2D.Float(x + thick / 2f, y + thick / 2f,
                    w - thick, h - thick, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(thick + 2, thick + 2, thick + 2, thick + 2);
        }
    }
}