package controller;

import java.time.format.DateTimeFormatter;

import database.DatabaseException;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Expense;
import service.ExpenseService;
import utility.ExpenseValidator;

public class EditExpenseController extends NavigationController {

    @FXML
    private TableView<Expense> expenseTable;

    @FXML
    private TableColumn<Expense, String> itemNameColumn;

    @FXML
    private TableColumn<Expense, String> categoryColumn;

    @FXML
    private TableColumn<Expense, String> currencyColumn;

    @FXML
    private TableColumn<Expense, Double> amountColumn;

    @FXML
    private TableColumn<Expense, Double> phpValueColumn;

    @FXML
    private TableColumn<Expense, String> dateAddedColumn;

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

    private final ExpenseService expenseService = ExpenseService.getInstance();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    private void initialize() {
        currencyComboBox.getItems().setAll(expenseService.getCurrencies());
        categoryComboBox.getItems().setAll(expenseService.getCategories());

        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        phpValueColumn.setCellValueFactory(new PropertyValueFactory<>("convertedPhpValue"));
        dateAddedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDateAdded().format(dateFormatter)));

        expenseTable.setItems(expenseService.getExpenses());

        try {
            expenseService.refreshExpenses();
        } catch (DatabaseException exception) {
            showMessage(exception.getMessage(), true);
        }

        expenseTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, selectedExpense) -> populateForm(selectedExpense));
    }

    @FXML
    private void handleUpdateExpense() {
        Expense selectedExpense = expenseTable.getSelectionModel().getSelectedItem();

        if (selectedExpense == null) {
            showMessage("Please select an expense to edit.", true);
            return;
        }

        String validationMessage = ExpenseValidator.validate(itemNameField.getText(), amountField.getText(),
                currencyComboBox.getValue(), categoryComboBox.getValue());

        if (!validationMessage.isEmpty()) {
            showMessage(validationMessage, true);
            return;
        }

        double amount = Double.parseDouble(amountField.getText().trim());

        try {
            expenseService.updateExpense(selectedExpense, itemNameField.getText(), categoryComboBox.getValue(),
                    currencyComboBox.getValue(), amount);
            expenseTable.refresh();
            showMessage("Expense updated successfully in Supabase.", false);
        } catch (DatabaseException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    @FXML
    private void handleDeleteSelected() {
        Expense selectedExpense = expenseTable.getSelectionModel().getSelectedItem();

        if (selectedExpense == null) {
            showMessage("Please select an expense to delete.", true);
            return;
        }

        try {
            expenseService.deleteExpense(selectedExpense);
            clearForm();
            showMessage("Selected expense deleted from Supabase.", false);
        } catch (DatabaseException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    @FXML
    private void handleDeleteAll() {
        if (expenseService.getExpenses().isEmpty()) {
            showMessage("There are no expenses to delete.", true);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete All Expenses");
        confirmation.setHeaderText("Delete all expenses?");
        confirmation.setContentText("This will remove every expense in the current session.");

        confirmation.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> deleteAllExpenses());
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        expenseTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    private void populateForm(Expense expense) {
        if (expense == null) {
            return;
        }

        itemNameField.setText(expense.getItemName());
        amountField.setText(String.valueOf(expense.getAmount()));
        currencyComboBox.setValue(expense.getCurrency());
        categoryComboBox.setValue(expense.getCategory());
        messageLabel.setText("");
    }

    private void clearForm() {
        itemNameField.clear();
        amountField.clear();
        currencyComboBox.setValue("PHP");
        categoryComboBox.setValue(null);
    }

    private void deleteAllExpenses() {
        try {
            expenseService.deleteAllExpenses();
            clearForm();
            showMessage("All expenses deleted from Supabase.", false);
        } catch (DatabaseException exception) {
            showMessage(exception.getMessage(), true);
        }
    }

    private void showMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("error-text", "success-text");
        messageLabel.getStyleClass().add(error ? "error-text" : "success-text");
    }
}
