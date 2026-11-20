package ru.college.attendance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.college.attendance.R
import ru.college.attendance.data.StudentEntity

class StudentAdapter(
    private val onDelete: (StudentEntity) -> Unit
) : RecyclerView.Adapter<StudentAdapter.VH>() {

    private var items: List<StudentEntity> = emptyList()

    fun submit(list: List<StudentEntity>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvGroup: TextView = view.findViewById(R.id.tvGroup)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val student = items[position]
        holder.tvName.text = "${student.lastName} ${student.firstName}"
        holder.tvGroup.text = "Группа: ${student.groupName}"
        holder.btnDelete.setOnClickListener { onDelete(student) }
    }

    override fun getItemCount(): Int = items.size
}