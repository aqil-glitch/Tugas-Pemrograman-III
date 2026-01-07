package com.example.project_notepad.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_notepad.R
import com.example.project_notepad.data.Note
import android.widget.PopupMenu



class NoteAdapter(
    private var notes: List<Note>,
    private val onClick: (Note) -> Unit,
    private val onDelete: (Note) -> Unit,
    private val onShare: (Note) -> Unit,
    private val onDetail: (Note) -> Unit

) : RecyclerView.Adapter<NoteAdapter.Holder>() {

    fun updateData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val btnItemMenu: ImageView = view.findViewById(R.id.btnItemMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val note = notes[position]
        holder.tvTitle.text = note.title
        holder.tvContent.text = note.content

        holder.itemView.setOnClickListener {
            onClick(note)
        }

        holder.btnItemMenu.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnItemMenu)
            popup.inflate(R.menu.note_item_menu)

            popup.setOnMenuItemClickListener{
                when (it.itemId) {
                    R.id.action_share -> {
                        onShare(note)
                        true
                    }
                    R.id.action_delete -> {
                        onDelete(note)
                        true
                    }
                    R.id.action_detail -> {
                        onDetail(note)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

    }

    override fun getItemCount() = notes.size
}
