package utility;

public final class ExpenseValidator {

    private ExpenseValidator() {
    }

    public static String validate(String itemName, String amountText, String currency, String category) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return "Please enter an expense item name.";
        }

        if (amountText == null || amountText.trim().isEmpty()) {
            return "Please enter an expense amount.";
        }

        try {
            double amount = Double.parseDouble(amountText.trim());

            if (amount <= 0) {
                return "Amount must be greater than zero.";
            }
        } catch (NumberFormatException exception) {
            return "Amount must be a valid number.";
        }

        if (currency == null || currency.isBlank()) {
            return "Please select a currency.";
        }

        if (category == null || category.isBlank()) {
            return "Please select a category.";
        }

        return "";
    }
}
