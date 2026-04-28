package com.expensetracker.service;

import com.expensetracker.model.Budget;
import com.expensetracker.util.AppConfig;
import com.expensetracker.util.JsonUtil;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BudgetService {

    private List<Budget> all = new ArrayList<>();

    public BudgetService() { load(); }

    public void set(Budget b) {
        all.removeIf(x -> x.getUsername().equals(b.getUsername())
                       && x.getCategory().equals(b.getCategory())
                       && x.getMonth().equals(b.getMonth()));
        all.add(b);
        save();
    }

    public Budget get(String username, String category, String month) {
        return all.stream()
               .filter(b -> b.getUsername().equals(username)
                         && b.getCategory().equals(category)
                         && b.getMonth().equals(month))
               .findFirst().orElse(null);
    }

    public List<Budget> getByUserAndMonth(String username, String month) {
        List<Budget> result = new ArrayList<>();
        for (Budget b : all)
            if (b.getUsername().equals(username) && b.getMonth().equals(month))
                result.add(b);
        return result;
    }

    private void load() {
        File f = new File(AppConfig.BUDGETS_FILE);
        if (!f.exists()) return;
        try {
            String json = new String(Files.readAllBytes(f.toPath()));
            List<Map<String, Object>> list = JsonUtil.parseArray(json);
            for (Map<String, Object> m : list) {
                Budget b = new Budget();
                b.setUsername(str(m, "username"));
                b.setCategory(str(m, "category"));
                Object lim = m.get("limit");
                b.setLimit(lim instanceof Number ? ((Number) lim).doubleValue() : 0);
                b.setMonth(str(m, "month"));
                all.add(b);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Budget b : all) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("username", b.getUsername());
            m.put("category", b.getCategory());
            m.put("limit", b.getLimit());
            m.put("month", b.getMonth());
            list.add(m);
        }
        try {
            Files.write(Paths.get(AppConfig.BUDGETS_FILE),
                JsonUtil.pretty(JsonUtil.toJson(list)).getBytes());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key); return v != null ? v.toString() : "";
    }
}