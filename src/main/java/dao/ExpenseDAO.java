package dao;

import java.util.List;

import model.Expense;

public interface ExpenseDAO {

    Expense insert(Expense expense);

    List<Expense> findAll();

    void update(Expense expense);

    void deleteById(long id);

    void deleteAll();

    boolean testConnection();
}
