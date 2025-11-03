package com.example.expensetracker2.data.repository

import com.example.expensetracker2.data.local.ExpenseDao
import com.example.expensetracker2.data.model.Expense
import com.example.expensetracker2.data.remote.ApiManager
import com.example.expensetracker2.data.remote.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val firebaseManager: FirebaseManager,
    apiManager: ApiManager
) {

    // ====================
    // Local Operations (Room)
    // ====================

    fun getAllExpenses(userId: String): Flow<List<Expense>> {
        return expenseDao.getAllExpenses(userId)
    }

    suspend fun getExpenseById(expenseId: String): Expense? {
        return withContext(Dispatchers.IO) {
            expenseDao.getExpenseById(expenseId)
        }
    }

    suspend fun insertExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.insertExpense(expense)
        }
    }

    suspend fun updateExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            val updatedExpense = expense.copy(
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            expenseDao.updateExpense(updatedExpense)
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.deleteExpense(expense)
        }
    }

    // ====================
    // Cloud Sync Operations
    // ====================

    suspend fun addExpenseWithSync(expense: Expense): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Save to local database first
                expenseDao.insertExpense(expense)

                // 2. Try to sync to cloud (Firebase)
                try {
                    val syncResult = firebaseManager.syncExpenseToCloud(expense)

                    if (syncResult.isSuccess) {
                        // Mark as synced if cloud sync succeeds
                        expenseDao.markAsSynced(expense.id)
                    }
                } catch (e: Exception) {
                    // Cloud sync failed, but local save succeeded
                    // Continue without error - will sync later
                    e.printStackTrace()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateExpenseWithSync(expense: Expense): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Update locally
                val updatedExpense = expense.copy(
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
                expenseDao.updateExpense(updatedExpense)

                // Sync to cloud
                val syncResult = firebaseManager.updateExpenseInCloud(updatedExpense)

                if (syncResult.isSuccess) {
                    expenseDao.markAsSynced(expense.id)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteExpenseWithSync(expense: Expense): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Delete from local
                expenseDao.deleteExpense(expense)

                // Delete from cloud
                firebaseManager.deleteExpenseFromCloud(expense.id)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun syncAllExpenses(userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Get unsynced local expenses
                val unsyncedExpenses = expenseDao.getUnsyncedExpenses(userId)

                // 2. Upload to cloud
                if (unsyncedExpenses.isNotEmpty()) {
                    val result = firebaseManager.syncMultipleExpenses(unsyncedExpenses)

                    if (result.isSuccess) {
                        // Mark all as synced
                        unsyncedExpenses.forEach { expense ->
                            expenseDao.markAsSynced(expense.id)
                        }
                    }
                }

                // 3. Fetch from cloud and merge
                val cloudResult = firebaseManager.fetchExpensesFromCloud(userId)

                if (cloudResult.isSuccess) {
                    val cloudExpenses = cloudResult.getOrNull() ?: emptyList()
                    expenseDao.insertExpenses(cloudExpenses)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchExpensesFromCloud(userId: String): Result<List<Expense>> {
        return withContext(Dispatchers.IO) {
            firebaseManager.fetchExpensesFromCloud(userId)
        }
    }

    // ====================
    // Analytics
    // ====================

    fun getTotalByCategory(userId: String) = expenseDao.getTotalByCategory(userId)

    suspend fun getTotalInDateRange(userId: String, startDate: Long, endDate: Long): Double {
        return withContext(Dispatchers.IO) {
            expenseDao.getTotalInDateRange(userId, startDate, endDate) ?: 0.0
        }
    }
}