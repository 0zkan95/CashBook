package com.example.cashbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun getAll(): Flow<List<Transaction>>

    // Running total across everything — this is your "current cash balance".
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions")
    fun getCurrentBalance(): Flow<Double>

    @Query("SELECT * FROM transactions WHERE amount >= 0 ORDER BY timestampMillis DESC")
    fun getIncome(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE amount < 0 ORDER BY timestampMillis DESC")
    fun getExpense(): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount >= 0")
    fun getIncomeBalance(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount < 0")
    fun getExpenseBalance(): Flow<Double>

    @Query("SELECT * FROM transactions WHERE amount >= 0 AND timestampMillis BETWEEN :start AND :end ORDER BY timestampMillis DESC")
    fun getIncomeInRange(start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE amount < 0 AND timestampMillis BETWEEN :start AND :end ORDER BY timestampMillis DESC")
    fun getExpenseInRange(start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount >= 0 AND timestampMillis BETWEEN :start AND :end")
    fun getIncomeBalanceInRange(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount < 0 AND timestampMillis BETWEEN :start AND :end")
    fun getExpenseBalanceInRange(start: Long, end: Long): Flow<Double>

    // Generic range query — reuse this same query for day/week/month/year by
    // just passing different start/end millis computed in your ViewModel.
    @Query(
        """
        SELECT * FROM transactions
        WHERE timestampMillis BETWEEN :startMillis AND :endMillis
        ORDER BY timestampMillis DESC
        """
    )
    fun getTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE timestampMillis BETWEEN :startMillis AND :endMillis
        """
    )
    fun getBalanceInRange(startMillis: Long, endMillis: Long): Flow<Double>
}