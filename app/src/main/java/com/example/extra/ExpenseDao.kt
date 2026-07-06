package com.example.extra

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

// Flow: Self updating data stream
import kotlinx.coroutines.flow.Flow

// Dao: Data Access Object - Interface for database operations
@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses WHERE jornadaId IS NULL ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Int): Flow<Expense>

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Int)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Transaction
    @Query("SELECT * FROM expenses WHERE jornadaId IS NULL ORDER BY createdAt DESC")
    fun getAllExpensesWithCategories(): Flow<List<ExpenseWithCategories>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE jornadaId IS NULL ORDER BY amount DESC")
    fun getAllExpensesByAmountDesc(): Flow<List<ExpenseWithCategories>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE jornadaId IS NULL ORDER BY amount ASC")
    fun getAllExpensesByAmountAsc(): Flow<List<ExpenseWithCategories>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE jornadaId IS NULL ORDER BY name COLLATE NOCASE ASC")
    fun getAllExpensesByNameAsc(): Flow<List<ExpenseWithCategories>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE jornadaId IS NULL ORDER BY name COLLATE NOCASE DESC")
    fun getAllExpensesByNameDesc(): Flow<List<ExpenseWithCategories>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseWithCategoriesById(id: Int): Flow<ExpenseWithCategories?>

    @Query("SELECT expenses.* FROM expenses INNER JOIN expense_category_join ON expenses.id = expense_category_join.expenseId WHERE expense_category_join.categoryId = :categoryId AND expenses.jornadaId IS NULL")
    fun getExpensesForCategory(categoryId: Int): Flow<List<Expense>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE jornadaId = :jornadaId ORDER BY createdAt DESC")
    fun getExpensesWithCategoriesForJornada(jornadaId: Int): Flow<List<ExpenseWithCategories>>

    @Query("UPDATE expenses SET jornadaId = :jornadaId WHERE jornadaId IS NULL")
    suspend fun closeCurrentSession(jornadaId: Int)

    @Query("UPDATE expenses SET jornadaId = :jornadaId WHERE id IN (:ids)")
    suspend fun assignExpensesToJornada(ids: List<Int>, jornadaId: Int)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}

// !! Suspend means the function runs in a coroutine