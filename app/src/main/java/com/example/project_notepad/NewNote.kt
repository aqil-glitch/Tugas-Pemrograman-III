package com.example.project_notepad

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project_notepad.data.Note
import com.example.project_notepad.data.NoteDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class NewNote : AppCompatActivity() {

    override fun onCreate (savedInstanceStat: Bundle?) {
        super.onCreate(savedInstanceStat)
        setContentView(R.layout.new_note)

        val noteArea = findViewById<EditText>(R.id.noteArea)
        val btnSave = findViewById<TextView>(R.id.btnsave)
        val titleArea = findViewById<EditText>(R.id.titleArea)


        val db = NoteDataBase.getInstance(this)

        val noteId = intent.getIntExtra("note_id", -1)

        if (noteId != -1) {
            lifecycleScope.launch {
                val note = db.noteDao().getById(noteId) ?: return@launch
                titleArea.setText(note.title)
                noteArea.setText(note.content)
            }
        }

        btnSave.setOnClickListener {
            val title = titleArea.text.toString().trim()
            val content = noteArea.text.toString().trim()

            if (title.isEmpty() && content.isEmpty()){
                finish()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                if (noteId == -1) {
                    // INSERT BARU
                    val note = Note(
                        title = title,
                        content = content
                    )
                    db.noteDao().insert(note)
                }else {
                    val oldNote = db.noteDao().getById(noteId) ?: return@launch

                    val updatedNote = oldNote.copy(
                        title = title,
                        content = content,
                        updatedAt = System.currentTimeMillis()
                    )

                    db.noteDao().update(updatedNote)
                }
                finish()
            }
        }
    }
}