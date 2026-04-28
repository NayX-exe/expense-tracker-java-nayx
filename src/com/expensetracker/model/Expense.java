package com.expensetracker.model;

public class Expense {
    private String id;
    private String username;
    private String title;
    private double amount;
    private String category;
    private String date; // yyyy-MM-dd
    private String note;
    private String type; // "expense" or "income"
    private String createdAt;

    public Expense() {}

    public Expense(String id, String username, String title, double amount,
                   String category, String date, String note, String type, String createdAt) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.type = type;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}