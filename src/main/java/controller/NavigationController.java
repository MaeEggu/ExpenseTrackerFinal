package controller;

import application.MainApp;

public class NavigationController {

    public void showHome() {
        MainApp.showView("/fxml/home.fxml");
    }

    public void showAddExpense() {
        MainApp.showView("/fxml/add_expense.fxml");
    }

    public void showViewExpense() {
        MainApp.showView("/fxml/view_expense.fxml");
    }

    public void showEditExpense() {
        MainApp.showView("/fxml/edit_expense.fxml");
    }
}
