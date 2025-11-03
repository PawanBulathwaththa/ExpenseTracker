package com.example.expensetracker2.data.local

import androidx.room.*
import com.example.expensetracker2.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // Insert or replace expense
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    // Insert multiple expenses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    // Get all expenses for a user (as Flow for real-time updates)
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: String): Flow<List<Expense>>

    // Get expense by ID
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): Expense?

    // Get unsynced expenses (for cloud sync)
    @Query("SELECT * FROM expenses WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedExpenses(userId: String): List<Expense>

    // Update expense
    @Update
    suspend fun updateExpense(expense: Expense)

    // Delete expense
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Delete all expenses for a user
    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun deleteAllExpenses(userId: String)

    // Mark expense as synced
    @Query("UPDATE expenses SET isSynced = 1 WHERE id = :expenseId")
    suspend fun markAsSynced(expenseId: String)

    // Get total spending by category
    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE userId = :userId GROUP BY category")
    fun getTotalByCategory(userId: String): Flow<List<CategoryTotal>>

    // Get total spending in a date range
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalInDateRange(userId: String, startDate: Long, endDate: Long): Double?
}

// Helper data class for category totals
data class CategoryTotal(
    val category: String,
    val total: Double
)