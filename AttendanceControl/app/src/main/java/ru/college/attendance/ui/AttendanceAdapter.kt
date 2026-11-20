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

class AttendanceAdapter(
    private val onStatusChanged: () -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.VH>() {

    private var items: List<StudentEntity> = emptyList()
    private val statuses = mutableMapOf<Int, String>()

    fun submit(students: List<StudentEntity>, savedStatuses: Map<Int, String>) {
        items = students
        statuses.clear()
        students.forEach { student ->
            statuses[student.id] = savedStatuses[student.id] ?: AttendanceStatus.PRESENT
        }
        notifyDataSetChanged()
        onStatusChanged()
    }

    fun getStatuses(): Map<Int, String> = statuses.toMap()

    fun getStatistics(): Triple<Int, Int, Int> {
        val present = statuses.values.count { it == AttendanceStatus.PRESENT }
        val absent = statuses.values.count { it == AttendanceStatus.ABSENT }
        val late = statuses.values.count { it == AttendanceStatus.LATE }
        return Triple(present, absent, late)
    }

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

        holder.rgStatus.setOnCheckedChangeListener { _, newCheckedId ->
            val newStatus = when (newCheckedId) {
                R.id.rbAbsent -> AttendanceStatus.ABSENT
                R.id.rbLate -> AttendanceStatus.LATE
                else -> AttendanceStatus.PRESENT
            }
            statuses[student.id] = newStatus
            onStatusChanged()
        }
    }

    override fun getItemCount(): Int = items.size
}