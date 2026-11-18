package ru.college.attendance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.college.attendance.R
import ru.college.attendance.data.AttendanceStatus
import ru.college.attendance.data.StudentEntity

/** Список студентов занятия: у каждого RadioGroup со статусом. */
class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.VH>() {

    private var items: List<StudentEntity> = emptyList()
    private val statuses = mutableMapOf<Int, String>()

    /** По умолчанию всем студентам выставляется «Присутствовал». */
    fun submit(students: List<StudentEntity>) {
        items = students
        statuses.clear()
        students.forEach { statuses[it.id] = AttendanceStatus.PRESENT }
        notifyDataSetChanged()
    }

    fun getStatuses(): Map<Int, String> = statuses.toMap()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val rgStatus: RadioGroup = view.findViewById(R.id.rgStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val student = items[position]
        holder.tvName.text = "${student.lastName} ${student.firstName}"

        holder.rgStatus.setOnCheckedChangeListener(null)
        holder.rgStatus.check(
            when (statuses[student.id]) {
                AttendanceStatus.ABSENT -> R.id.rbAbsent
                AttendanceStatus.LATE -> R.id.rbLate
                else -> R.id.rbPresent
            }
        )
        holder.rgStatus.setOnCheckedChangeListener { _, checkedId ->
            statuses[student.id] = when (checkedId) {
                R.id.rbAbsent -> AttendanceStatus.ABSENT
                R.id.rbLate -> AttendanceStatus.LATE
                else -> AttendanceStatus.PRESENT
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
