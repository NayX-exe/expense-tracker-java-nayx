package com.expensetracker.model;

public class Budget {
    private String username;
    private String category;
    private double limit;
    private String month; // yyyy-MM

    public Budget() {}

    public Budget(String username, String category, double limit, String month) {
        this.username = username;
        this.category = category;
        this.limit = limit;
        this.month = month;
    }

    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }
    public double getLimit() { return limit; }
    public void setLimit(double l) { this.limit = l; }
    public String getMonth() { return month; }
    public void setMonth(String m) { this.month = m; }
}