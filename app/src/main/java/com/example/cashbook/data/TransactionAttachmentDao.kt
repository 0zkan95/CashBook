package com.example.cashbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionAttachmentDao {
    @Query("SELECT * FROM transaction_attachments WHERE transactionId = :transactionId")
    fun getAttachmentsForTransaction(transactionId: Long): Flow<List<TransactionAttachment>>

    @Insert
    suspend fun insertAll(attachments: List<TransactionAttachment>)

    @Delete
    suspend fun delete(attachment: TransactionAttachment)

    @Query("DELETE FROM transaction_attachments WHERE transactionId = :transactionId")
    suspend fun clearForTransaction(transactionId: Long)
}
