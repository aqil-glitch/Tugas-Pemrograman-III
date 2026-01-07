package com.example.project_notepad

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope

import com.google.android.material.navigation.NavigationView
import com.example.project_notepad.data.NoteDataBase
import com.example.project_notepad.adapter.NoteAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: NoteDataBase
    private lateinit var adapter: NoteAdapter
    private lateinit var noteCountText: TextView
    private var isSearchVisible = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        val btnSearch = findViewById<ImageView>(R.id.btnSearch)
        val btnSearchInput = findViewById<EditText>(R.id.btnSearchInput)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val recyclerView = findViewById<RecyclerView>(R.id.noteRecyclerView)
        noteCountText = findViewById<TextView>(R.id.noteCount)



        //Load Data
        recyclerView.layoutManager = LinearLayoutManager(this)
        db = NoteDataBase.getInstance(this)

        adapter = NoteAdapter(
            emptyList(),

            onClick = { note ->
                val intent = Intent(this, NewNote::class.java)
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            },

            onDelete = { note ->
                lifecycleScope.launch {
                    db.noteDao().moveToTrash(
                        note.id,
                        System.currentTimeMillis()
                    )
                    loadNotes()
                }
            },

            onShare = { note ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${note.title}\n\n${note.content}"
                    )
                }
                startActivity(Intent.createChooser(intent, "Share Note"))
            },

            onDetail = { note ->
                showDetailDialog(note)
            }
        )


        recyclerView.adapter = adapter

        // text cari
        btnSearchInput.addTextChangedListener(object : android.text.TextWatcher {

            override fun afterTextChanged(s: android.text.Editable?) {
                val keyword = s.toString().trim()

                lifecycleScope.launch {
                    val notes = if (keyword.isEmpty()) {
                        db.noteDao().getActiveNotes()
                    } else {
                        db.noteDao().searchNotes(keyword)
                    }

                    adapter.updateData(notes)

                    // scroll ke atas
                    recyclerView.scrollToPosition(0)

                    noteCountText.text = "${notes.size} Catatan"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // tombol cari
        btnSearch.setOnClickListener {

            isSearchVisible = !isSearchVisible

            if (isSearchVisible) {

                btnSearchInput.visibility = View.VISIBLE
                btnSearchInput.requestFocus()

            } else {

                btnSearchInput.visibility = View.GONE
                btnSearchInput.text.clear()

                loadNotes()
            }
        }

        // tombol menu
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        // tambah catatan
        btnAdd.setOnClickListener {
            val intent = Intent(this, NewNote::class.java)
            startActivity(intent)
        }

        // klik menu
        navigationView.setNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.menu_profile -> {
                    val intent =  Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                //R.id.menu_notes -> {
                    // TODO:
                //}
                R.id.menu_trash -> {
                    startActivity(Intent(this, TrashActivity::class.java))
                }
            }

            drawerLayout.closeDrawers()

            true
        }
    }
    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun showDetailDialog(note: com.example.project_notepad.data.Note){
        val message = """
            judul : ${note.title}
            isi : ${note.content.length} karakter
            Dibuat : ${formatDate(note.createdAt)}
            Terakhir Diupdate : ${formatDate(note.updatedAt)}
            Terakhir Diupdate : ${note.updatedAt}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detail Catatan")
            .setMessage(message)
            .setPositiveButton("OK",null)
            .show()

    }


    override fun onResume() {
        super.onResume()
        loadNotes()
    }
    private fun loadNotes() {
        lifecycleScope.launch {
            val notes = db.noteDao().getActiveNotes()
            adapter.updateData(notes)

            // jumlah catatan
            noteCountText.text = "${notes.size} Catatan"
        }
    }
}