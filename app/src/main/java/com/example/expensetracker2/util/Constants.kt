package com.example.expensetracker2.util

object Constants {

    // Expense Categories
    val EXPENSE_CATEGORIES = listOf(
        "Food & Drinks",
        "Transport",
        "Shopping",
        "Entertainment",
        "Bills & Utilities",
        "Healthcare",
        "Education",
        "Groceries",
        "Other"
    )

    // Date formats
    const val DATE_FORMAT = "dd MMM yyyy"
    const val TIME_FORMAT = "hh:mm a"
    const val DATE_TIME_FORMAT = "dd MMM yyyy, hh:mm a"

    // Shared Preferences
    const val PREF_NAME = "expense_tracker_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"

    // Firestore Collections
    const val COLLECTION_EXPENSES = "expenses"
    const val COLLECTION_USERS = "users"

    // API Configuration
    // TODO: Replace with your actual API URL
    const val BASE_URL = "http://192.168.1.101/expense_tracker/api/"

    // Sync Settings
    const val SYNC_SOURCE_FIREBASE = "firebase"
    const val SYNC_SOURCE_PHP = "php"
    const val CURRENT_SYNC_SOURCE = SYNC_SOURCE_PHP // Change to SYNC_SOURCE_PHP when ready
}