package utility;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyConverter implements Converter {

    private static final String API_URL = "https://api.frankfurter.dev/v1/latest?base=PHP&symbols=USD,EUR,JPY";
    private static final Pattern DATE_PATTERN = Pattern.compile("\\\"date\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern RATE_PATTERN = Pattern.compile("\\\"(USD|EUR|JPY)\\\"\\s*:\\s*([0-9.]+)");

    // Fallback rates represent the PHP value of one unit of each supported currency.
    private static final Map<String, Double> FALLBACK_PHP_EXCHANGE_RATES = Map.of(
            "PHP", 1.00,
            "USD", 56.50,
            "EUR", 61.50,
            "JPY", 0.38);

    private final Map<String, Double> phpExchangeRates;
    private String lastExchangeRateUpdateLabel;

    public CurrencyConverter() {
        phpExchangeRates = new LinkedHashMap<>(FALLBACK_PHP_EXCHANGE_RATES);
        lastExchangeRateUpdateLabel = "Manual fallback rates";
        refreshExchangeRates();
    }

    @Override
    public double toPhp(double amount, String currency) {
        Double rate = phpExchangeRates.get(currency);

        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        return amount * rate;
    }

    public List<String> getSupportedCurrencies() {
        return List.of("PHP", "USD", "EUR", "JPY");
    }

    public String getLastExchangeRateUpdateLabel() {
        return lastExchangeRateUpdateLabel;
    }

    public void refreshExchangeRates() {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                lastExchangeRateUpdateLabel = "Manual fallback rates";
                return;
            }

            Map<String, Double> latestRates = parsePhpRates(response.body());

            if (!latestRates.isEmpty()) {
                phpExchangeRates.clear();
                phpExchangeRates.putAll(latestRates);
                lastExchangeRateUpdateLabel = "Frankfurter latest rates" + parseDateSuffix(response.body());
            }
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            lastExchangeRateUpdateLabel = "Manual fallback rates";
        }
    }

    private Map<String, Double> parsePhpRates(String json) {
        Map<String, Double> parsedRates = new LinkedHashMap<>();
        parsedRates.put("PHP", 1.00);

        Matcher matcher = RATE_PATTERN.matcher(json);

        while (matcher.find()) {
            String currency = matcher.group(1);
            double currencyPerPhp = Double.parseDouble(matcher.group(2));

            if (currencyPerPhp > 0) {
                parsedRates.put(currency, 1 / currencyPerPhp);
            }
        }

        return parsedRates.size() == getSupportedCurrencies().size() ? parsedRates : Map.of();
    }

    private String parseDateSuffix(String json) {
        Matcher matcher = DATE_PATTERN.matcher(json);

        if (matcher.find()) {
            return " (" + matcher.group(1) + ")";
        }

        return "";
    }
}