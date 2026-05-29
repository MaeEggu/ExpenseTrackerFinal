package utility;

import java.util.List;
import java.util.Map;

public class CurrencyConverter implements Converter {

    // Rates represent the PHP value of one unit of each supported currency.
    private static final Map<String, Double> PHP_EXCHANGE_RATES = Map.of(
            "PHP", 1.00,
            "USD", 56.50,
            "EUR", 61.50,
            "JPY", 0.38);

    @Override
    public double toPhp(double amount, String currency) {
        Double rate = PHP_EXCHANGE_RATES.get(currency);

        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        return amount * rate;
    }

    public List<String> getSupportedCurrencies() {
        return List.of("PHP", "USD", "EUR", "JPY");
    }
}
