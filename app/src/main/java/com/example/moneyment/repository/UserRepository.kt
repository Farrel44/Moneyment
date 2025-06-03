package com.example.moneyment.repository

import android.util.Log
import com.example.moneyment.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val USERS_COLLECTION = "user"
        private const val TAG = "UserRepository"
    }

    suspend fun saveUserToFirestore(firebaseUser: FirebaseUser): Boolean {
        return try {
            val user = User(
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user)
                .await()
            
            Log.d(TAG, "User saved to Firestore successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore", e)
            false
        }
    }

    suspend fun updateUserInFirestore(firebaseUser: FirebaseUser): Boolean {
        return try {
            val updates = mapOf(
                "name" to (firebaseUser.displayName ?: ""),
                "email" to (firebaseUser.email ?: ""),
                "profileImageUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                "updatedAt" to Timestamp.now()
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .update(updates)
                .await()
            
            Log.d(TAG, "User updated in Firestore successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user in Firestore", e)
            false
        }
    }

    suspend fun getUserFromFirestore(userId: String): User? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user from Firestore", e)
            null
        }
    }

    suspend fun checkIfUserExists(userId: String): Boolean {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            document.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user exists", e)
            false
        }
    }

    suspend fun saveOrUpdateUser(firebaseUser: FirebaseUser): Boolean {
        return try {
            val userExists = checkIfUserExists(firebaseUser.uid)
            
            if (userExists) {
                updateUserInFirestore(firebaseUser)
            } else {
                saveUserToFirestore(firebaseUser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in saveOrUpdateUser", e)
            false
        }
    }
}
