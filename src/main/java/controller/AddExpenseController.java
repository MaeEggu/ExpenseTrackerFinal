package controller;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import database.DatabaseException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Expense;
import service.ExpenseService;
import utility.ExpenseValidator;

public class AddExpenseController extends NavigationController {

    @FXML
    private TextField itemNameField;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> currencyComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    private Label conversionPreviewLabel;

    @FXML
    private Label todayExpensesValueLabel;

    @FXML
    private Label monthlyTotalValueLabel;

    @FXML
    private Label topCategoryValueLabel;

    private final ExpenseService expenseService = ExpenseService.getInstance();
    private final NumberFormat phpFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "PH"));

    @FXML
    private void initialize() {
        currencyComboBox.getItems().setAll(expenseService.getCurrencies());
        categoryComboBox.getItems().setAll(expenseService.getCategories());
        currencyComboBox.setValue("PHP");

        amountField.textProperty().addListener((observable, oldValue, newValue) -> updateConversionPreview());
        currencyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateConversionPreview());
        expenseService.getExpenses().addListener((javafx.collections.ListChangeListener<Expense>) change -> updateStats());

        updateConversionPreview();
        updateStats();

        if (!expenseService.getLastDatabaseMessage().isBlank()
                && !expenseService.getLastDatabaseMessage().equals("Connected to Supabase.")) {
            showMessage(expenseService.getLastDatabaseMessage(), true);
        }
    }

    @FXML
    private void handleAddExpense() {
        String validationMessage = ExpenseValidator.validate(itemNameField.getText(), amountField.getText(),
                currencyComboBox.getValue(), categoryComboBox.getValue());

        if (!validationMessage.isEmpty()) {
            showMessage(validationMessage, true);
            return;
        }

        double amount = Double.parseDouble(amountField.getText().trim());

        try {
            expenseService.addExpense(itemNameField.getText(), categoryComboBox.getValue(), currencyComboBox.getValue(),
                    amount);
            clearForm();
            updateStats();
            showMessage("Expense added successfully and saved to Supabase.", false);
        } catch (DatabaseException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        messageLabel.setText("");
    }

    private void clearForm() {
        itemNameField.clear();
        amountField.clear();
        currencyComboBox.setValue("PHP");
        categoryComboBox.setValue(null);
        updateConversionPreview();
    }

    private void updateConversionPreview() {
        if (conversionPreviewLabel == null) {
            return;
        }

        String amountText = amountField.getText();
        String currency = currencyComboBox.getValue();

        if (amountText == null || amountText.isBlank()) {
            conversionPreviewLabel.setText("Enter an amount to preview the converted PHP value.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText.trim());

            if (amount <= 0 || currency == null) {
                conversionPreviewLabel.setText("Use a positive amount and select a currency.");
                return;
            }

            double phpValue = expenseService.convertToPhp(amount, currency);
            conversionPreviewLabel.setText(currency + " " + String.format("%.2f", amount)
                    + " equals " + phpFormat.format(phpValue));
        } catch (NumberFormatException exception) {
            conversionPreviewLabel.setText("Amount must be a valid number.");
        }
    }

    private void updateStats() {
        if (todayExpensesValueLabel == null || monthlyTotalValueLabel == null || topCategoryValueLabel == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        double todayTotal = expenseService.getExpenses().stream()
                .filter(expense -> today.equals(expense.getDateAdded()))
                .mapToDouble(Expense::getConvertedPhpValue)
                .sum();

        double monthlyTotal = expenseService.getExpenses().stream()
                .filter(expense -> YearMonth.from(expense.getDateAdded()).equals(currentMonth))
                .mapToDouble(Expense::getConvertedPhpValue)
                .sum();

        String topCategory = expenseService.getTotalPhpByCategory().entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(entry -> entry.getKey() + " - " + phpFormat.format(entry.getValue()))
                .orElse("No expenses yet");

        todayExpensesValueLabel.setText(phpFormat.format(todayTotal));
        monthlyTotalValueLabel.setText(phpFormat.format(monthlyTotal));
        topCategoryValueLabel.setText(topCategory);
    }

    private void showMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-text", "success-text");
        messageLabel.getStyleClass().add(error ? "error-text" : "success-text");
    }
}
