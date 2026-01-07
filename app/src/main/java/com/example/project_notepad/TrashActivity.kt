package com.example.project_notepad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project_notepad.adapter.NoteAdapter
import com.example.project_notepad.data.Note
import com.example.project_notepad.data.NoteDataBase
import kotlinx.coroutines.launch

class TrashActivity : AppCompatActivity() {

    private lateinit var db : NoteDataBase
    private lateinit var adapter: NoteAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        val recyclerView = findViewById<RecyclerView>(R.id.trashRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        db = NoteDataBase.getInstance(this)

        adapter = NoteAdapter(
            emptyList(),

            onClick = { note ->
                showTrashDetail(note)
            },

            onDelete = { note ->
                lifecycleScope.launch {
                    db.noteDao().deleteForever(note.id)
                    loadTrash()
                }
            },

            onShare = { },

            onDetail = { note ->
                showTrashDetail(note)
            }
        )
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadTrash()
    }

    private fun loadTrash() {
        lifecycleScope.launch {
            val trashNotes = db.noteDao().getTrashNotes()
            adapter.updateData(trashNotes)
        }
    }

    private fun showTrashDetail(note: Note){
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detail Catatan")
            .setMessage(
                """
                    ${note.title}
                    
                    ${note.content}
                """.trimIndent()
            )
            .setPositiveButton("Restore") { _, _ ->
                lifecycleScope.launch {
                    db.noteDao().restoreNote(note.id)
                    loadTrash()
                }
            }
            .setNegativeButton("Delete Forever") { _, _ ->
                lifecycleScope.launch {
                    db.noteDao().deleteForever(note.id)
                    loadTrash()
                }
            }
            .setNeutralButton("Batal", null)
            .show()

    }
}