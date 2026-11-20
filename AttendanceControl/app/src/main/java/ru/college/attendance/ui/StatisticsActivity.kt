package ru.college.attendance.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.college.attendance.R
import ru.college.attendance.data.*

class StatisticsActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.get(this) }
    private lateinit var spGroup: Spinner
    private lateinit var tvResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        spGroup = findViewById(R.id.spGroup)
        tvResults = findViewById(R.id.tvResults)

        loadGroups()
    }

    private fun loadGroups() {
        lifecycleScope.launch {
            val groups = db.studentDao().getGroups()
            if (groups.isEmpty()) {
                tvResults.text = "Нет данных для статистики. Сначала добавьте студентов."
                return@launch
            }
            spGroup.adapter = ArrayAdapter(this@StatisticsActivity, android.R.layout.simple_spinner_dropdown_item, groups)
            spGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    calculateStats(groups[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun calculateStats(group: String) {
        lifecycleScope.launch {
            val students = db.studentDao().getByGroup(group)
            if (students.isEmpty()) {
                tvResults.text = "В группе $group нет студентов"
                return@launch
            }

            val studentIds = students.map { it.id }
            val allRecords = db.attendanceDao().getAll().filter { it.studentId in studentIds }

            if (allRecords.isEmpty()) {
                tvResults.text = "Для группы $group еще нет отметок"
                return@launch
            }

            val totalLessons = allRecords.map { "${it.lessonDate}_${it.subject}" }.distinct().size
            val present = allRecords.count { it.status == AttendanceStatus.PRESENT }
            val absent = allRecords.count { it.status == AttendanceStatus.ABSENT }
            val late = allRecords.count { it.status == AttendanceStatus.LATE }

            tvResults.text = """
                Группа: $group
                Всего проведено занятий: $totalLessons
                
                Присутствовали: $present
                Отсутствовали: $absent
                Опоздали: $late
                
                Всего отметок: ${allRecords.size}
            """.trimIndent()
        }
    }
}