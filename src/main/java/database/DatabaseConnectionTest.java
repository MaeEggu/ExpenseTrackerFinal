package database;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        try (var connection = DatabaseConnection.getConnection()) {
            System.out.println("Connected to Supabase PostgreSQL.");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("User: " + connection.getMetaData().getUserName());
        } catch (Exception exception) {
            System.err.println("Connection failed: " + exception.getMessage());
        }
    }
}
