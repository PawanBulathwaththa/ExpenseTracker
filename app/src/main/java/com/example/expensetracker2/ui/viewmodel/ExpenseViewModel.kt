package com.example.expensetracker2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker2.data.local.CategoryTotal
import com.example.expensetracker2.data.model.Expense
import com.example.expensetracker2.data.repository.ExpenseRepository
import com.example.expensetracker2.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val context: Context
) : ViewModel() {

    private val _expenseState = MutableStateFlow<ExpenseState>(ExpenseState.Idle)
    val expenseState: StateFlow<ExpenseState> = _expenseState.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _categoryTotals = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val categoryTotals: StateFlow<List<CategoryTotal>> = _categoryTotals.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun loadExpenses(userId: String) {
        viewModelScope.launch {
            repository.getAllExpenses(userId).collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }

    fun loadCategoryTotals(userId: String) {
        viewModelScope.launch {
            repository.getTotalByCategory(userId).collect { totals ->
                _categoryTotals.value = totals
            }
        }
    }

    fun addExpense(
        userId: String,
        amount: Double,
        category: String,
        note: String,
        photoUri: String? = null,
        date: Date = Date()
    ) {
        if (amount <= 0) {
            _expenseState.value = ExpenseState.Error("Amount must be greater than 0")
            return
        }

        if (category.isBlank()) {
            _expenseState.value = ExpenseState.Error("Please select a category")
            return
        }

        viewModelScope.launch {
            _expenseState.value = ExpenseState.Loading

            val expense = Expense(
                amount = amount,
                category = category,
                note = note,
                photoUri = photoUri,
                date = date,
                userId = userId,
                isSynced = false
            )

            val result = repository.addExpenseWithSync(expense)

            _expenseState.value = if (result.isSuccess) {
                ExpenseState.Success("Expense added successfully!")
            } else {
                ExpenseState.Error(result.exceptionOrNull()?.message ?: "Failed to add expense")
            }
        }
    }

    fun updateExpense(expense: Expense) {
        if (expense.amount <= 0) {
            _expenseState.value = ExpenseState.Error("Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            _expenseState.value = ExpenseState.Loading

            val result = repository.updateExpenseWithSync(expense)

            _expenseState.value = if (result.isSuccess) {
                ExpenseState.Success("Expense updated successfully!")
            } else {
                ExpenseState.Error(result.exceptionOrNull()?.message ?: "Failed to update expense")
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            _expenseState.value = ExpenseState.Loading

            val result = repository.deleteExpenseWithSync(expense)

            _expenseState.value = if (result.isSuccess) {
                ExpenseState.Success("Expense deleted successfully!")
            } else {
                ExpenseState.Error(result.exceptionOrNull()?.message ?: "Failed to delete expense")
            }
        }
    }

    fun syncExpenses(userId: String) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _expenseState.value = ExpenseState.Error("No internet connection. Changes will sync when online.")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true

            val result = repository.syncAllExpenses(userId)

            _isSyncing.value = false

            _expenseState.value = if (result.isSuccess) {
                ExpenseState.Success("Expenses synced successfully!")
            } else {
                ExpenseState.Error(result.exceptionOrNull()?.message ?: "Sync failed")
            }
        }
    }

    suspend fun getTotalInDateRange(userId: String, startDate: Long, endDate: Long): Double {
        return repository.getTotalInDateRange(userId, startDate, endDate)
    }

    fun resetExpenseState() {
        _expenseState.value = ExpenseState.Idle
    }
}

sealed class ExpenseState {
    object Idle : ExpenseState()
    object Loading : ExpenseState()
    data class Success(val message: String) : ExpenseState()
    data class Error(val message: String) : ExpenseState()
}