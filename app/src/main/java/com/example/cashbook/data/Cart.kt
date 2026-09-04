package com.example.cashbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A cart is a planned group of purchases. It starts as "open" and, once you
 * check out, becomes a single Transaction (see cartId link in Transaction.kt).
 */
@Entity(tableName = "carts")
data class Cart(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAtMillis: Long,
    val checkedOut: Boolean = false
)

/**
 * One line item inside a cart, e.g. "Milk - $3.50".
 */
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cartId: Long,
    val name: String,
    val price: Double,
    val quantity: Int = 1
)