package com.example.expensetracker2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetracker2.data.local.ExpenseDatabase
import com.example.expensetracker2.data.remote.FirebaseManager
import com.example.expensetracker2.data.repository.ExpenseRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val firebaseManager by lazy { FirebaseManager() }

    private val expenseRepository by lazy {
        val database = ExpenseDatabase.getDatabase(context)
        ExpenseRepository(database.expenseDao(), firebaseManager)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(firebaseManager) as T
            }
            modelClass.isAssignableFrom(ExpenseViewModel::class.java) -> {
                ExpenseViewModel(expenseRepository, context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}