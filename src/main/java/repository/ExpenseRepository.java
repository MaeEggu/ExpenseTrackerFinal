package repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.ExpenseDAO;
import database.DatabaseConnection;
import database.DatabaseException;
import model.Expense;

public class ExpenseRepository implements ExpenseDAO {

    private static final String INSERT_SQL = """
            INSERT INTO expenses (item_name, category, currency, amount, converted_php_value, date_added)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT id, item_name, category, currency, amount, converted_php_value, date_added
            FROM expenses
            ORDER BY date_added DESC, id DESC
            """;

    private static final String UPDATE_SQL = """
            UPDATE expenses
            SET item_name = ?, category = ?, currency = ?, amount = ?, converted_php_value = ?
            WHERE id = ?
            """;

    private static final String DELETE_BY_ID_SQL = "DELETE FROM expenses WHERE id = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM expenses";

    @Override
    public Expense insert(Expense expense) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            bindInsertFields(statement, expense);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    expense.setId(resultSet.getLong("id"));
                }
            }

            return expense;
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to insert expense into Supabase.", exception);
        }
    }

    @Override
    public List<Expense> findAll() {
        List<Expense> expenses = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                expenses.add(mapExpense(resultSet));
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to retrieve expenses from Supabase.", exception);
        }

        return expenses;
    }

    @Override
    public void update(Expense expense) {
        if (expense.getId() == null) {
            throw new DatabaseException("Unable to update expense because it has no database ID.");
        }

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bindUpdateFields(statement, expense);
            statement.setLong(6, expense.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to update expense in Supabase.", exception);
        }
    }

    @Override
    public void deleteById(long id) {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to delete selected expense from Supabase.", exception);
        }
    }

    @Override
    public void deleteAll() {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_ALL_SQL)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Unable to delete all expenses from Supabase.", exception);
        }
    }

    @Override
    public boolean testConnection() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException exception) {
            throw new DatabaseException("Database connection test failed.", exception);
        }
    }

    private void bindInsertFields(PreparedStatement statement, Expense expense) throws SQLException {
        statement.setString(1, expense.getItemName());
        statement.setString(2, expense.getCategory());
        statement.setString(3, expense.getCurrency());
        statement.setDouble(4, expense.getAmount());
        statement.setDouble(5, expense.getConvertedPhpValue());
        statement.setDate(6, Date.valueOf(expense.getDateAdded()));
    }

    private void bindUpdateFields(PreparedStatement statement, Expense expense) throws SQLException {
        statement.setString(1, expense.getItemName());
        statement.setString(2, expense.getCategory());
        statement.setString(3, expense.getCurrency());
        statement.setDouble(4, expense.getAmount());
        statement.setDouble(5, expense.getConvertedPhpValue());
    }

    private Expense mapExpense(ResultSet resultSet) throws SQLException {
        return new Expense(
                resultSet.getLong("id"),
                resultSet.getString("item_name"),
                resultSet.getString("category"),
                resultSet.getString("currency"),
                resultSet.getDouble("amount"),
                resultSet.getDouble("converted_php_value"),
                resultSet.getDate("date_added").toLocalDate());
    }
}
