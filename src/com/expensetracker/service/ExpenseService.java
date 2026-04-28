package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.util.AppConfig;
import com.expensetracker.util.JsonUtil;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseService {

    private List<Expense> all = new ArrayList<>();

    public ExpenseService() { load(); }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    public void add(Expense e) {
        if (e.getId() == null || e.getId().isEmpty())
            e.setId(UUID.randomUUID().toString());
        if (e.getCreatedAt() == null)
            e.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        all.add(e);
        save();
    }

    public void update(Expense updated) {
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(updated.getId())) {
                all.set(i, updated);
                break;
            }
        }
        save();
    }

    public void delete(String id) {
        all.removeIf(e -> e.getId().equals(id));
        save();
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public List<Expense> getByUser(String username) {
        return all.stream().filter(e -> e.getUsername().equals(username))
                  .sorted(Comparator.comparing(Expense::getDate).reversed())
                  .collect(Collectors.toList());
    }

    public List<Expense> getByUserAndMonth(String username, String yearMonth) {
        return getByUser(username).stream()
               .filter(e -> e.getDate().startsWith(yearMonth))
               .collect(Collectors.toList());
    }

    public List<Expense> getByUserAndYear(String username, String year) {
        return getByUser(username).stream()
               .filter(e -> e.getDate().startsWith(year))
               .collect(Collectors.toList());
    }

    public double totalExpenses(List<Expense> list) {
        return list.stream().filter(e -> "expense".equals(e.getType()))
                   .mapToDouble(Expense::getAmount).sum();
    }

    public double totalIncome(List<Expense> list) {
        return list.stream().filter(e -> "income".equals(e.getType()))
                   .mapToDouble(Expense::getAmount).sum();
    }

    public Map<String, Double> expenseByCategory(List<Expense> list) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Expense e : list) {
            if ("expense".equals(e.getType()))
                map.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        return map.entrySet().stream()
                  .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                           (a, b) -> a, LinkedHashMap::new));
    }

    /** Returns monthly totals for a year: key = "yyyy-MM", value = [income, expense] */
    public Map<String, double[]> monthlyTotals(String username, String year) {
        Map<String, double[]> map = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String key = year + "-" + String.format("%02d", m);
            map.put(key, new double[]{0, 0});
        }
        for (Expense e : getByUserAndYear(username, year)) {
            String key = e.getDate().substring(0, 7);
            if (!map.containsKey(key)) map.put(key, new double[]{0, 0});
            if ("income".equals(e.getType())) map.get(key)[0] += e.getAmount();
            else                              map.get(key)[1] += e.getAmount();
        }
        return map;
    }

    public List<String> getMonthsWithData(String username) {
        Set<String> months = new TreeSet<>(Comparator.reverseOrder());
        for (Expense e : getByUser(username))
            months.add(e.getDate().substring(0, 7));
        return new ArrayList<>(months);
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    private void load() {
        File f = new File(AppConfig.EXPENSES_FILE);
        if (!f.exists()) return;
        try {
            String json = new String(Files.readAllBytes(f.toPath()));
            List<Map<String, Object>> list = JsonUtil.parseArray(json);
            for (Map<String, Object> m : list) all.add(mapToExpense(m));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Expense e : all) list.add(expenseToMap(e));
        try {
            Files.write(Paths.get(AppConfig.EXPENSES_FILE),
                JsonUtil.pretty(JsonUtil.toJson(list)).getBytes());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Map<String, Object> expenseToMap(Expense e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("username", e.getUsername());
        m.put("title", e.getTitle());
        m.put("amount", e.getAmount());
        m.put("category", e.getCategory());
        m.put("date", e.getDate());
        m.put("note", e.getNote() != null ? e.getNote() : "");
        m.put("type", e.getType());
        m.put("createdAt", e.getCreatedAt());
        return m;
    }

    private Expense mapToExpense(Map<String, Object> m) {
        Expense e = new Expense();
        e.setId(str(m, "id"));
        e.setUsername(str(m, "username"));
        e.setTitle(str(m, "title"));
        Object amt = m.get("amount");
        e.setAmount(amt instanceof Number ? ((Number) amt).doubleValue() : 0);
        e.setCategory(str(m, "category"));
        e.setDate(str(m, "date"));
        e.setNote(str(m, "note"));
        e.setType(str(m, "type"));
        e.setCreatedAt(str(m, "createdAt"));
        return e;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }
}