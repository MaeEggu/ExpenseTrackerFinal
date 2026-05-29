package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Expense Tracker");
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(640);
        showView("/fxml/home.fxml");
        primaryStage.show();
    }

    public static void showView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource(fxmlPath));

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root, 1100, 720));
            } else {
                primaryStage.getScene().setRoot(root);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load view: " + fxmlPath, exception);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
