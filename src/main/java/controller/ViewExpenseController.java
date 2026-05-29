package controller;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import database.DatabaseException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Expense;
import service.ExpenseService;

public class ViewExpenseController extends NavigationController {

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
    private Label totalExpensesLabel;

    @FXML
    private Label categorySummaryLabel;

    @FXML
    private PieChart categoryPieChart;

    private final ExpenseService expenseService = ExpenseService.getInstance();
    private final NumberFormat phpFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "PH"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    private void initialize() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        phpValueColumn.setCellValueFactory(new PropertyValueFactory<>("convertedPhpValue"));

        dateAddedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateAdded().format(dateFormatter)));

        expenseTable.setItems(expenseService.getExpenses());

        try {
            expenseService.refreshExpenses();
        } catch (DatabaseException exception) {
            totalExpensesLabel.setText("Total Expenses: ₱0.00");
            categorySummaryLabel.setText(exception.getMessage());
            categorySummaryLabel.getStyleClass().add("error-text");
            categoryPieChart.setData(FXCollections.observableArrayList());
            return;
        }

        updateSummary();
    }

    private void updateSummary() {

        totalExpensesLabel.setText(
                "Total Expenses: " +
                        phpFormat.format(expenseService.getTotalPhp()));

        Map<String, Double> categoryTotals = expenseService.getTotalPhpByCategory();

        if (categoryTotals.isEmpty()) {
            categorySummaryLabel.setText("No expenses added yet.");
            categoryPieChart.setData(FXCollections.observableArrayList());
            return;
        }

        StringBuilder summaryBuilder = new StringBuilder();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            summaryBuilder.append(entry.getKey())
                    .append(": ")
                    .append(phpFormat.format(entry.getValue()))
                    .append(System.lineSeparator());
        }

        categorySummaryLabel.setText(summaryBuilder.toString().trim());

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        categoryPieChart.setData(pieData);
    }
}