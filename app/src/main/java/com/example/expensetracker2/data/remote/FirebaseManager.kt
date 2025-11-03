package com.example.expensetracker2.data.remote


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.expensetracker2.data.model.Expense
import com.example.expensetracker2.util.Constants
import kotlinx.coroutines.tasks.await

class FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ====================
    // Authentication
    // ====================

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ====================
    // Firestore - Expenses
    // ====================

    suspend fun syncExpenseToCloud(expense: Expense): Result<Unit> {
        return try {
            val expenseMap = hashMapOf(
                "id" to expense.id,
                "amount" to expense.amount,
                "category" to expense.category,
                "note" to expense.note,
                "photoUri" to expense.photoUri,
                "date" to expense.date.time,
                "userId" to expense.userId,
                "createdAt" to expense.createdAt,
                "updatedAt" to expense.updatedAt
            )

            firestore.collection(Constants.COLLECTION_EXPENSES)
                .document(expense.id)
                .set(expenseMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncMultipleExpenses(expenses: List<Expense>): Result<Unit> {
        return try {
            val batch = firestore.batch()

            expenses.forEach { expense ->
                val expenseMap = hashMapOf(
                    "id" to expense.id,
                    "amount" to expense.amount,
                    "category" to expense.category,
                    "note" to expense.note,
                    "photoUri" to expense.photoUri,
                    "date" to expense.date.time,
                    "userId" to expense.userId,
                    "createdAt" to expense.createdAt,
                    "updatedAt" to expense.updatedAt
                )

                val docRef = firestore.collection(Constants.COLLECTION_EXPENSES)
                    .document(expense.id)
                batch.set(docRef, expenseMap)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchExpensesFromCloud(userId: String): Result<List<Expense>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_EXPENSES)
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val expenses = snapshot.documents.mapNotNull { doc ->
                try {
                    Expense(
                        id = doc.getString("id") ?: return@mapNotNull null,
                        amount = doc.getDouble("amount") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        note = doc.getString("note") ?: "",
                        photoUri = doc.getString("photoUri"),
                        date = java.util.Date(doc.getLong("date") ?: 0L),
                        userId = doc.getString("userId") ?: "",
                        isSynced = true,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        updatedAt = doc.getLong("updatedAt") ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(expenses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpenseFromCloud(expenseId: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_EXPENSES)
                .document(expenseId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExpenseInCloud(expense: Expense): Result<Unit> {
        return syncExpenseToCloud(expense)
    }
}