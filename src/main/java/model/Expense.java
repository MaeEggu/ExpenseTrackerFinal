package model;

import java.time.LocalDate;

public class Expense {

    // Encapsulated fields keep expense data controlled through getters and setters.
    private Long id;
    private String itemName;
    private String category;
    private String currency;
    private double amount;
    private double convertedPhpValue;
    private LocalDate dateAdded;

    public Expense(Long id, String itemName, String category, String currency, double amount, double convertedPhpValue,
            LocalDate dateAdded) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.currency = currency;
        this.amount = amount;
        this.convertedPhpValue = convertedPhpValue;
        this.dateAdded = dateAdded;
    }

    public Expense(String itemName, String category, String currency, double amount, double convertedPhpValue,
            LocalDate dateAdded) {
        this(null, itemName, category, currency, amount, convertedPhpValue, dateAdded);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getConvertedPhpValue() {
        return convertedPhpValue;
    }

    public void setConvertedPhpValue(double convertedPhpValue) {
        this.convertedPhpValue = convertedPhpValue;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }
}
