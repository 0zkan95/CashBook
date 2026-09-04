package com.example.cashbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Transaction::class, Cart::class, CartItem::class, Category::class, TransactionItem::class, TransactionAttachment::class, UserAccount::class, Note::class, NoteItem::class],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun cartDao(): CartDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionItemDao(): TransactionItemDao
    abstract fun transactionAttachmentDao(): TransactionAttachmentDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cash_tracker.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}