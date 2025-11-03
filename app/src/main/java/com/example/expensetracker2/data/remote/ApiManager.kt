package com.example.expensetracker2.data.remote

import com.example.expensetracker2.data.model.Expense
import java.util.Date

class ApiManager {

    private val apiService = RetrofitClient.apiService

    suspend fun addExpense(expense: Expense): Result<Unit> {
        return try {
            val request = expense.toRequest()
            val response = apiService.addExpense(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to add expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            val request = expense.toRequest()
            val response = apiService.updateExpense(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            val response = apiService.deleteExpense(expenseId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchExpenses(userId: String): Result<List<Expense>> {
        return try {
            val response = apiService.getExpenses(userId)

            if (response.isSuccessful && response.body()?.success == true) {
                val expenses = response.body()?.data?.map { it.toExpense() } ?: emptyList()
                Result.success(expenses)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch expenses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncExpenses(userId: String, localExpenses: List<Expense>): Result<List<Expense>> {
        return try {
            val request = SyncRequest(
                userId = userId,
                expenses = localExpenses.map { it.toRequest() }
            )

            val response = apiService.syncExpenses(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val serverExpenses = response.body()?.serverExpenses?.map { it.toExpense() } ?: emptyList()
                Result.success(serverExpenses)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Extension functions for conversion
    private fun Expense.toRequest() = ExpenseRequest(
        id = id,
        amount = amount,
        category = category,
        note = note,
        photoUri = photoUri,
        date = date.time,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ExpenseData.toExpense() = Expense(
        id = id,
        amount = amount,
        category = category,
        note = note,
        photoUri = photoUri,
        date = Date(date),
        userId = userId,
        isSynced = true,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}