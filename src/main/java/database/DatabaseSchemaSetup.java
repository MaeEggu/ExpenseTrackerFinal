package database;

import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseSchemaSetup {

    public static void main(String[] args) {
        Path schemaPath = Path.of("database", "schema.sql");

        try {
            String schemaSql = Files.readString(schemaPath);

            try (var connection = DatabaseConnection.getConnection();
                    var statement = connection.createStatement()) {
                // The schema script only creates the table/indexes if they do not already exist.
                statement.execute(schemaSql);
            }

            System.out.println("Supabase schema setup completed.");
        } catch (Exception exception) {
            System.err.println("Schema setup failed: " + exception.getMessage());
        }
    }
}
