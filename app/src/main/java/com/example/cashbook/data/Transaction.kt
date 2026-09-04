package com.example.cashbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single cash movement (income or expense).
 * All daily/weekly/monthly/yearly balances are calculated FROM this table —
 * we never store separate "period balance" rows, we just query and sum.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Store as epoch millis (UTC). Easiest to query/sort/group by date this way.
    val timestampMillis: Long,

    // Positive for income, negative for expense — this makes SUM() give you
    // the net balance directly without extra CASE/WHEN logic in queries.
    val amount: Double,

    val category: String,

    val note: String = "",

    // Nullable link back to a Cart, set once a cart is "checked out" into a real transaction.
    val cartId: Long? = null,
    val hasBills: Boolean = false,
    val hasItems: Boolean = false
)
