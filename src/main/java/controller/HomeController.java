package controller;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import service.ExpenseService;

public class HomeController extends NavigationController {

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label databaseStatusValueLabel;

    @FXML
    private Label databaseProviderValueLabel;

    @FXML
    private Label totalRecordsValueLabel;

    @FXML
    private Label totalExpensesValueLabel;

    @FXML
    private Label totalTransactionsValueLabel;

    @FXML
    private Label recentExpenseDateValueLabel;

    @FXML
    private VBox activityTimelineBox;

    private final ExpenseService expenseService = ExpenseService.getInstance();
    private final NumberFormat phpFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "PH"));
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy - hh:mm a");
    private Timeline clock;

    @FXML
    private void initialize() {
        updateDateTime();
        refreshOverview();
        buildActivityTimeline();

        clock = new Timeline(new KeyFrame(Duration.minutes(1), event -> updateDateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateDateTime() {
        dateTimeLabel.setText(LocalDateTime.now().format(dateTimeFormatter));
    }

    private void refreshOverview() {
        boolean connected = "Connected to Supabase.".equals(expenseService.getLastDatabaseMessage());

        databaseStatusValueLabel.setText(connected ? "Connected" : "Disconnected");
        databaseProviderValueLabel.setText("Supabase PostgreSQL");
        totalRecordsValueLabel.setText(String.valueOf(expenseService.getExpenses().size()));

        totalExpensesValueLabel.setText(phpFormat.format(expenseService.getTotalPhp()));
        totalTransactionsValueLabel.setText(String.valueOf(expenseService.getExpenses().size()));
        recentExpenseDateValueLabel.setText(expenseService.getMostRecentExpenseDateLabel());
    }

    private void buildActivityTimeline() {
        activityTimelineBox.getChildren().clear();

        if (expenseService.getRecentActivities().isEmpty()) {
            Label emptyLabel = new Label("No recent activity yet.");
            emptyLabel.getStyleClass().add("activity-empty");
            activityTimelineBox.getChildren().add(emptyLabel);
            return;
        }

        int limit = Math.min(8, expenseService.getRecentActivities().size());

        for (int index = 0; index < limit; index++) {
            Label activityLabel = new Label(expenseService.getRecentActivities().get(index));
            activityLabel.setWrapText(true);
            activityLabel.getStyleClass().add("activity-item");
            activityTimelineBox.getChildren().add(activityLabel);
        }
    }
}