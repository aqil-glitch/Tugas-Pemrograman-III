package com.example.project_notepad.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query(""" SELECT * FROM notes WHERE isDeleted = 0 ORDER BY id DESC """)
    suspend fun getActiveNotes(): List<Note>

    @Query(""" SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC """)
    suspend fun getTrashNotes(): List<Note>

    @Query(""" UPDATE notes SET isDeleted = 1, deletedAt = :time WHERE id = :id """)
    suspend fun moveToTrash(id: Int, time: Long)

    @Query(""" UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :id """)
    suspend fun restoreNote(id: Int)

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' AND isDeleted = 0")
    suspend fun searchNotes(query: String): List<Note>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteForever(id: Int)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Int): Note?
}

