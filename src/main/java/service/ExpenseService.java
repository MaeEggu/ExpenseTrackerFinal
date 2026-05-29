package service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dao.ExpenseDAO;
import database.DatabaseException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Expense;
import repository.ExpenseRepository;
import utility.CurrencyConverter;

public class ExpenseService {

    private static final ExpenseService INSTANCE = new ExpenseService();

    private final ObservableList<Expense> expenses;
    private final ObservableList<String> recentActivities;
    private final CurrencyConverter currencyConverter;
    private final ExpenseDAO expenseRepository;
    private String lastDatabaseMessage;

    private ExpenseService() {
        // One shared observable list lets all screens see the same current-session data.
        expenses = FXCollections.observableArrayList();
        recentActivities = FXCollections.observableArrayList();
        currencyConverter = new CurrencyConverter();
        expenseRepository = new ExpenseRepository();
        lastDatabaseMessage = "";
        loadExpensesSafely();
    }

    public static ExpenseService getInstance() {
        return INSTANCE;
    }

    public Expense addExpense(String itemName, String category, String currency, double amount) {
        double phpValue = currencyConverter.toPhp(amount, currency);
        Expense expense = new Expense(itemName.trim(), category, currency, amount, phpValue, LocalDate.now());
        Expense savedExpense = expenseRepository.insert(expense);
        expenses.add(0, savedExpense);
        addActivity("Added \"" + savedExpense.getItemName() + "\" - " + formatPeso(savedExpense.getConvertedPhpValue()));
        lastDatabaseMessage = "Connected to Supabase.";
        return savedExpense;
    }

    public void updateExpense(Expense expense, String itemName, String category, String currency, double amount) {
        if (expense == null) {
            return;
        }

        String originalItemName = expense.getItemName();
        expense.setItemName(itemName.trim());
        expense.setCategory(category);
        expense.setCurrency(currency);
        expense.setAmount(amount);
        expense.setConvertedPhpValue(currencyConverter.toPhp(amount, currency));
        expenseRepository.update(expense);
        addActivity("Updated \"" + originalItemName + "\"");

        // Replacing the same object at its index tells TableView listeners that this row changed.
        int index = expenses.indexOf(expense);
        if (index >= 0) {
            expenses.set(index, expense);
        }
    }

    public void deleteExpense(Expense expense) {
        if (expense == null) {
            return;
        }

        if (expense.getId() == null) {
            throw new DatabaseException("Unable to delete expense because it has no database ID.");
        }

        expenseRepository.deleteById(expense.getId());
        expenses.remove(expense);
        addActivity("Deleted \"" + expense.getItemName() + "\"");
    }

    public void deleteAllExpenses() {
        int deletedCount = expenses.size();
        expenseRepository.deleteAll();
        expenses.clear();
        addActivity("Deleted all expenses (" + deletedCount + " records)");
    }

    public void refreshExpenses() {
        expenses.setAll(expenseRepository.findAll());
        lastDatabaseMessage = "Connected to Supabase.";
    }

    public boolean testDatabaseConnection() {
        boolean connected = expenseRepository.testConnection();
        lastDatabaseMessage = connected ? "Connected to Supabase." : "Supabase connection test failed.";
        return connected;
    }

    public String getLastDatabaseMessage() {
        return lastDatabaseMessage;
    }

    public ObservableList<Expense> getExpenses() {
        return expenses;
    }

    public ObservableList<String> getRecentActivities() {
        return recentActivities;
    }

    public List<String> getCategories() {
        return List.of("Food", "Transportation", "School", "Bills", "Shopping", "Entertainment", "Others");
    }

    public List<String> getCurrencies() {
        return currencyConverter.getSupportedCurrencies();
    }

    public double convertToPhp(double amount, String currency) {
        return currencyConverter.toPhp(amount, currency);
    }

    public String getBaseCurrency() {
        return "PHP";
    }

    public String getLastExchangeRateUpdateLabel() {
        return "Manual rates";
    }

    public String getMostRecentExpenseDateLabel() {
        return expenses.stream()
                .map(Expense::getDateAdded)
                .max(LocalDate::compareTo)
                .map(date -> date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                .orElse("No records yet");
    }

    public double getTotalPhp() {
        return expenses.stream()
                .mapToDouble(Expense::getConvertedPhpValue)
                .sum();
    }

    public Map<String, Double> getTotalPhpByCategory() {
        Map<String, Double> totals = new LinkedHashMap<>();

        for (String category : getCategories()) {
            double categoryTotal = expenses.stream()
                    .filter(expense -> category.equals(expense.getCategory()))
                    .mapToDouble(Expense::getConvertedPhpValue)
                    .sum();

            if (categoryTotal > 0) {
                totals.put(category, categoryTotal);
            }
        }

        return totals;
    }

    private void loadExpensesSafely() {
        try {
            refreshExpenses();
        } catch (DatabaseException exception) {
            lastDatabaseMessage = exception.getMessage();
        }
    }

    private void addActivity(String activity) {
        recentActivities.add(0, activity);

        while (recentActivities.size() > 10) {
            recentActivities.remove(recentActivities.size() - 1);
        }
    }

    private String formatPeso(double value) {
        return java.text.NumberFormat.getCurrencyInstance(java.util.Locale.of("en", "PH")).format(value);
    }
}
