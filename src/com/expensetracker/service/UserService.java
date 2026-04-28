package com.expensetracker.service;

import com.expensetracker.model.User;
import com.expensetracker.util.AppConfig;
import com.expensetracker.util.JsonUtil;
import com.expensetracker.util.PasswordUtil;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserService {

    private Map<String, User> users = new LinkedHashMap<>();

    public UserService() { load(); }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    public User login(String username, String password) {
        User u = users.get(username.toLowerCase());
        if (u == null) return null;
        if (!PasswordUtil.verify(password, u.getPasswordHash())) return null;
        return u;
    }

    public boolean usernameExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public boolean emailExists(String email) {
        for (User u : users.values())
            if (u.getEmail().equalsIgnoreCase(email)) return true;
        return false;
    }

    public User register(String username, String password, String fullName, String email) {
        if (usernameExists(username)) return null;
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        User u = new User(username.toLowerCase(), PasswordUtil.hash(password), fullName, email, now);
        users.put(username.toLowerCase(), u);
        save();
        return u;
    }

    public void updateUser(User user) {
        users.put(user.getUsername(), user);
        save();
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    private void load() {
        File f = new File(AppConfig.USERS_FILE);
        if (!f.exists()) return;
        try {
            String json = new String(Files.readAllBytes(f.toPath()));
            List<Map<String, Object>> list = JsonUtil.parseArray(json);
            for (Map<String, Object> m : list) {
                User u = mapToUser(m);
                users.put(u.getUsername(), u);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users.values()) list.add(userToMap(u));
        try {
            Files.write(Paths.get(AppConfig.USERS_FILE),
                JsonUtil.pretty(JsonUtil.toJson(list)).getBytes());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Map<String, Object> userToMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", u.getUsername());
        m.put("passwordHash", u.getPasswordHash());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("createdAt", u.getCreatedAt());
        m.put("currency", u.getCurrency() != null ? u.getCurrency() : "USD");
        return m;
    }

    private User mapToUser(Map<String, Object> m) {
        User u = new User();
        u.setUsername(str(m, "username"));
        u.setPasswordHash(str(m, "passwordHash"));
        u.setFullName(str(m, "fullName"));
        u.setEmail(str(m, "email"));
        u.setCreatedAt(str(m, "createdAt"));
        u.setCurrency(m.containsKey("currency") ? str(m, "currency") : "USD");
        return u;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }
}