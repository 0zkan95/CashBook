package com.example.cashbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note_items WHERE noteId = :noteId")
    fun getItemsForNote(noteId: Long): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: NoteItem)

    @Update
    suspend fun updateItem(item: NoteItem)

    @Delete
    suspend fun deleteItem(item: NoteItem)

    @Query("DELETE FROM note_items WHERE noteId = :noteId")
    suspend fun deleteItemsForNote(noteId: Long)
}
