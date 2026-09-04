package com.example.cashbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photoUri: String? = null,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val gmail: String,
    val isActive: Boolean = false,
    val pin: String? = null // New field for app lock
)
