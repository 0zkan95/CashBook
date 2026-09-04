package com.example.cashbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isShoppingList: Boolean = false
)

@Entity(tableName = "note_items")
data class NoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val text: String,
    val isChecked: Boolean = false
)
