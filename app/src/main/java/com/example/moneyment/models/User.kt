package com.example.moneyment.models

import com.google.firebase.Timestamp

data class User(
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", Timestamp.now(), Timestamp.now())
}
