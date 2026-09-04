package com.example.cashbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionItemDao {
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    fun getItemsForTransaction(transactionId: Long): Flow<List<TransactionItem>>

    @Insert
    suspend fun insertAll(items: List<TransactionItem>)

    @Delete
    suspend fun delete(item: TransactionItem)

    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun clearForTransaction(transactionId: Long)
}
