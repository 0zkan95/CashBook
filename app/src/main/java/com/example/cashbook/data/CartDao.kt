package com.example.cashbook.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Insert
    suspend fun insertCart(cart: Cart): Long

    @Update
    suspend fun updateCart(cart: Cart)

    @Delete
    suspend fun deleteCart(cart: Cart)

    @Insert
    suspend fun insertItem(item: CartItem): Long

    @Delete
    suspend fun deleteItem(item: CartItem)

    @Query("SELECT * FROM carts WHERE checkedOut = 0 ORDER BY createdAtMillis DESC")
    fun getOpenCarts(): Flow<List<Cart>>

    @Query("SELECT * FROM cart_items WHERE cartId = :cartId")
    fun getItemsForCart(cartId: Long): Flow<List<CartItem>>

    @Query("SELECT COALESCE(SUM(price * quantity), 0) FROM cart_items WHERE cartId = :cartId")
    suspend fun getCartTotal(cartId: Long): Double
}