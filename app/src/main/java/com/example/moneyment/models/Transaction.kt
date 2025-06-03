package com.example.moneyment.models

import com.google.firebase.Timestamp

data class Transaction(
    val id: String = "",
    val amount: Int = 0,
    val date: Timestamp = Timestamp.now(),
    val note: String = "",
    val type: String = "", // "income" or "expenses"
    val userId: String = ""
) {
    // No-argument constructor for Firestore
    constructor() : this("", 0, Timestamp.now(), "", "", "")
    
    companion object {
        const val TYPE_INCOME = "income"
        const val TYPE_EXPENSES = "expenses"
    }
}
