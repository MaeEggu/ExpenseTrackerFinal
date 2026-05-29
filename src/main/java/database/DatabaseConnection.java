package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final String ENV_URL = "SUPABASE_DB_URL";
    private static final String ENV_USER = "SUPABASE_DB_USER";
    private static final String ENV_PASSWORD = "SUPABASE_DB_PASSWORD";
    private static final Path ENV_FILE_PATH = Path.of(".env");
    private static final Path LOCAL_CONFIG_PATH = Path.of("config", "database.properties");
    private static final String CLASSPATH_CONFIG = "/database.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        DatabaseConfig config = loadConfig();
        validateConfig(config);

        try {
            Properties connectionProperties = new Properties();
            connectionProperties.setProperty("user", config.user());
            connectionProperties.setProperty("password", config.password());
            connectionProperties.setProperty("sslmode", "require");

            return DriverManager.getConnection(config.url(), connectionProperties);
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to connect to Supabase PostgreSQL. Check your JDBC URL, user, password, and network connection.",
                    exception);
        }
    }

    private static DatabaseConfig loadConfig() {
        String envUrl = System.getenv(ENV_URL);
        String envUser = System.getenv(ENV_USER);
        String envPassword = System.getenv(ENV_PASSWORD);

        if (hasText(envUrl) && hasText(envUser) && hasText(envPassword)) {
            return new DatabaseConfig(envUrl.trim(), envUser.trim(), envPassword.trim());
        }

        Properties properties = new Properties();
        loadDotEnvProperties(properties);

        if (properties.isEmpty()) {
            loadLocalProperties(properties);
        }

        if (properties.isEmpty()) {
            loadClasspathProperties(properties);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (!hasText(url) || !hasText(user) || !hasText(password)) {
            throw new DatabaseException("Database credentials are not configured. Copy config/database.properties.example to config/database.properties, then add your Supabase JDBC values.");
        }

        return new DatabaseConfig(url.trim(), user.trim(), password.trim());
    }

    private static void validateConfig(DatabaseConfig config) {
        if (config.url().startsWith("https://")) {
            throw new DatabaseException("db.url must be a PostgreSQL JDBC URL, not the Supabase Project URL. Use Supabase > Connect > Session pooler, then format it as jdbc:postgresql://...");
        }

        if (!config.url().startsWith("jdbc:postgresql://")) {
            throw new DatabaseException("db.url must start with jdbc:postgresql://.");
        }

        if (config.password().startsWith("sbp_")) {
            throw new DatabaseException("db.password must be your Supabase database password, not a Supabase API key.");
        }
    }

    private static void loadDotEnvProperties(Properties properties) {
        if (!Files.exists(ENV_FILE_PATH)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(ENV_FILE_PATH)) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                int separatorIndex = trimmedLine.indexOf('=');

                if (separatorIndex <= 0) {
                    continue;
                }

                String key = trimmedLine.substring(0, separatorIndex).trim();
                String value = removeOptionalQuotes(trimmedLine.substring(separatorIndex + 1).trim());

                if (ENV_URL.equals(key)) {
                    properties.setProperty("db.url", value);
                } else if (ENV_USER.equals(key)) {
                    properties.setProperty("db.user", value);
                } else if (ENV_PASSWORD.equals(key)) {
                    properties.setProperty("db.password", value);
                }
            }
        } catch (IOException exception) {
            throw new DatabaseException("Unable to read " + ENV_FILE_PATH + ".", exception);
        }
    }

    private static String removeOptionalQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");

            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }

        return value;
    }

    private static void loadLocalProperties(Properties properties) {
        if (!Files.exists(LOCAL_CONFIG_PATH)) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(LOCAL_CONFIG_PATH)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new DatabaseException("Unable to read " + LOCAL_CONFIG_PATH + ".", exception);
        }
    }

    private static void loadClasspathProperties(Properties properties) {
        try (InputStream inputStream = DatabaseConnection.class.getResourceAsStream(CLASSPATH_CONFIG)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new DatabaseException("Unable to read database.properties from application resources.", exception);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record DatabaseConfig(String url, String user, String password) {
    }
}

