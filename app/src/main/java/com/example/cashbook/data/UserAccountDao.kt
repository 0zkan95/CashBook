package com.example.cashbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts")
    fun getAll(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccount(): Flow<UserAccount?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: UserAccount): Long

    @Update
    suspend fun update(account: UserAccount)

    @Delete
    suspend fun delete(account: UserAccount)

    @Query("UPDATE user_accounts SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE user_accounts SET isActive = 1 WHERE id = :id")
    suspend fun activateAccount(id: Long)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getCount(): Int
}
