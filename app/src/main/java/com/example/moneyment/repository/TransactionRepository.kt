package com.example.moneyment.repository

import android.util.Log
import com.example.moneyment.models.Transaction
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class TransactionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TRANSACTIONS_COLLECTION = "transaction"
        private const val TAG = "TransactionRepository"
    }

    suspend fun addTransaction(
        amount: Int,
        note: String,
        type: String,
        date: Timestamp = Timestamp.now()
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return false
            }

            val documentRef = firestore.collection(TRANSACTIONS_COLLECTION).document()
            val transaction = Transaction(
                id = documentRef.id,
                amount = amount,
                date = date,
                note = note,
                type = type,
                userId = currentUser.uid
            )
            
            documentRef.set(transaction).await()
            Log.d(TAG, "Transaction added successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding transaction", e)
            false
        }
    }    suspend fun getUserTransactions(userId: String? = null): List<Transaction> {
        return try {
            val targetUserId = userId ?: auth.currentUser?.uid
            if (targetUserId == null) {
                Log.e(TAG, "User not authenticated and no userId provided")
                return emptyList()
            }

            Log.d(TAG, "Fetching transactions for user: $targetUserId")
            Log.d(TAG, "Using collection: $TRANSACTIONS_COLLECTION")

            val querySnapshot = firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", targetUserId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d(TAG, "Firestore query completed. Documents found: ${querySnapshot.documents.size}")

            val transactions = mutableListOf<Transaction>()
            for (document in querySnapshot.documents) {
                Log.d(TAG, "Processing document: ${document.id}")
                Log.d(TAG, "Document data: ${document.data}")
                
                document.toObject(Transaction::class.java)?.let { transaction ->
                    Log.d(TAG, "Successfully parsed transaction: ${transaction.id}, type: ${transaction.type}, amount: ${transaction.amount}")
                    transactions.add(transaction)
                } ?: Log.w(TAG, "Failed to parse document ${document.id} to Transaction object")
            }
            
            Log.d(TAG, "Retrieved ${transactions.size} transactions")
            transactions
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user transactions", e)
            emptyList()
        }
    }    suspend fun deleteTransaction(transactionId: String): Boolean {
        return try {
            firestore.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .delete()
                .await()
            
            Log.d(TAG, "Transaction deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting transaction", e)
            false
        }
    }

    suspend fun updateTransaction(
        transactionId: String,
        amount: Int,
        note: String,
        type: String,
        date: Timestamp = Timestamp.now()
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return false
            }

            val transactionData = mapOf(
                "amount" to amount,
                "note" to note,
                "type" to type,
                "date" to date,
                "updatedAt" to Timestamp.now()
            )

            firestore.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .update(transactionData)
                .await()

            Log.d(TAG, "Transaction updated successfully: $transactionId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction", e)
            false
        }
    }

    suspend fun testFirestoreConnection(): Boolean {
        return try {
            Log.d(TAG, "Testing Firestore connection...")
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user for test")
                return false
            }

            // Try to get the collection
            val testQuery = firestore.collection(TRANSACTIONS_COLLECTION)
                .limit(1)
                .get()
                .await()
            
            Log.d(TAG, "Firestore connection test successful. Collection exists.")
            Log.d(TAG, "Total documents in collection: ${testQuery.documents.size}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore connection test failed", e)
            false
        }
    }

    data class MonthlySummary(
        val totalExpenses: Int,
        val totalIncome: Int
    )    suspend fun getMonthlySummary(userId: String? = null): MonthlySummary {
        return try {
            val targetUserId = userId ?: auth.currentUser?.uid
            if (targetUserId == null) {
                Log.e(TAG, "User not authenticated and no userId provided")
                return MonthlySummary(0, 0)
            }

            // Get start and end of current month
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = Timestamp(calendar.time)

            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val endOfMonth = Timestamp(calendar.time)

            Log.d(TAG, "Fetching monthly summary for user: $targetUserId")
            Log.d(TAG, "Date range: ${startOfMonth.toDate()} to ${endOfMonth.toDate()}")

            // Use simpler query to avoid composite index requirement
            val querySnapshot = firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", targetUserId)
                .get()
                .await()

            Log.d(TAG, "User transactions query completed. Documents found: ${querySnapshot.documents.size}")

            var totalExpenses = 0
            var totalIncome = 0
            var monthlyTransactionsCount = 0

            for (document in querySnapshot.documents) {
                document.toObject(Transaction::class.java)?.let { transaction ->
                    // Filter by date range in code
                    val transactionDate = transaction.date
                    if (transactionDate != null && 
                        transactionDate.toDate().after(startOfMonth.toDate()) && 
                        transactionDate.toDate().before(endOfMonth.toDate())) {
                        
                        monthlyTransactionsCount++
                        Log.d(TAG, "Processing monthly transaction: ${transaction.id}, type: ${transaction.type}, amount: ${transaction.amount}, date: ${transactionDate.toDate()}")
                        
                        when (transaction.type.lowercase()) {
                            "expenses" -> {
                                totalExpenses += transaction.amount
                                Log.d(TAG, "Added expense: ${transaction.amount}")
                            }
                            "income" -> {
                                totalIncome += transaction.amount
                                Log.d(TAG, "Added income: ${transaction.amount}")
                            }
                            else -> {
                                Log.w(TAG, "Unknown transaction type: ${transaction.type}")
                            }
                        }
                    } else {
                        Log.d(TAG, "Transaction outside date range: ${transaction.id}, date: ${transactionDate?.toDate()}")
                    }
                }
            }

            Log.d(TAG, "Monthly summary - Total transactions in range: $monthlyTransactionsCount, Expenses: $totalExpenses, Income: $totalIncome")
            MonthlySummary(totalExpenses, totalIncome)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly summary", e)
            MonthlySummary(0, 0)
        }
    }

    suspend fun testUserHasTransactions(userId: String? = null): Boolean {
        return try {
            val targetUserId = userId ?: auth.currentUser?.uid
            if (targetUserId == null) {
                Log.e(TAG, "User not authenticated and no userId provided")
                return false
            }

            Log.d(TAG, "Testing if user has any transactions...")
            val querySnapshot = firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", targetUserId)
                .limit(1)
                .get()
                .await()

            val hasTransactions = querySnapshot.documents.isNotEmpty()
            Log.d(TAG, "User has transactions: $hasTransactions")
            
            if (hasTransactions) {
                val transaction = querySnapshot.documents[0].toObject(Transaction::class.java)
                Log.d(TAG, "Sample transaction: ${transaction?.id}, type: ${transaction?.type}, amount: ${transaction?.amount}")
            }
            
            hasTransactions
        } catch (e: Exception) {
            Log.e(TAG, "Error testing user transactions", e)
            false
        }
    }
}
