package com.example.expensetracker2.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val amount: Double,

    val category: String,

    val note: String = "",

    val photoUri: String? = null, // Local file path or URL

    val date: Date = Date(),

    val userId: String = "", // Firebase user ID

    val isSynced: Boolean = false, // Track if synced to cloud

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)