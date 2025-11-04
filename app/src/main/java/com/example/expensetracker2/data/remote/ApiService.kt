package com.example.expensetracker2.data.remote


import com.example.expensetracker2.data.model.Expense
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("expenses/add.php")
    suspend fun addExpense(@Body expense: ExpenseRequest): Response<ApiResponse>

    @GET("expenses/get.php")
    suspend fun getExpenses(@Query("userId") userId: String): Response<ExpensesResponse>

    @POST("expenses/update.php")
    suspend fun updateExpense(@Body expense: ExpenseRequest): Response<ApiResponse>

    @POST("expenses/delete.php")
    suspend fun deleteExpense(@Query("id") expenseId: String): Response<ApiResponse>


    @POST("expenses/sync.php")
    suspend fun syncExpenses(@Body request: SyncRequest): Response<SyncResponse>
}

// Request/Response Models
data class ExpenseRequest(
    val id: String,
    val amount: Double,
    val category: String,
    val note: String,
    val photoUri: String?,
    val date: Long,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)

data class ExpensesResponse(
    val success: Boolean,
    val data: List<ExpenseData>?,
    val message: String?
)

data class ExpenseData(
    val id: String,
    val amount: Double,
    val category: String,
    val note: String,
    val photoUri: String?,
    val date: Long,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class SyncRequest(
    val userId: String,
    val expenses: List<ExpenseRequest>
)

data class SyncResponse(
    val success: Boolean,
    val message: String,
    val serverExpenses: List<ExpenseData>?
)