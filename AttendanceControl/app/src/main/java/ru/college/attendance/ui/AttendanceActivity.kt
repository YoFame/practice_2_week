package ru.college.attendance.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.college.attendance.R
import ru.college.attendance.data.*
import ru.college.attendance.logic.DateTools

class AttendanceActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.get(this) }
    private val adapter by lazy { AttendanceAdapter { updateStatistics() } }

    private var currentGroup: String = ""
    private var currentLesson: LessonEntity? = null
    private var currentStudents: List<StudentEntity> = emptyList()

    private lateinit var spGroup: Spinner
    private lateinit var spLesson: Spinner
    private lateinit var btnAddLesson: Button
    private lateinit var btnDeleteLesson: Button
    private lateinit var tvEmpty: TextView
    private lateinit var tvStats: TextView
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        spGroup = findViewById(R.id.spGroup)
        spLesson = findViewById(R.id.spLesson)
        btnAddLesson = findViewById(R.id.btnAddLesson)
        btnDeleteLesson = findViewById(R.id.btnDeleteLesson)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvStats = findViewById(R.id.tvStats)
        btnSave = findViewById(R.id.btnSave)

        findViewById<RecyclerView>(R.id.rvMarks).apply {
            layoutManager = LinearLayoutManager(this@AttendanceActivity)
            adapter = this@AttendanceActivity.adapter
        }

        btnAddLesson.setOnClickListener { showAddLessonDialog() }
        btnDeleteLesson.setOnClickListener { deleteCurrentLesson() }
        btnSave.setOnClickListener { save() }

        loadGroups()
    }

    private fun loadGroups() {
        lifecycleScope.launch {
            val groups = db.studentDao().getGroups()
            if (groups.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                spGroup.visibility = View.GONE
                btnAddLesson.isEnabled = false
                btnDeleteLesson.isEnabled = false
                btnSave.isEnabled = false
                return@launch
            }
            spGroup.adapter = ArrayAdapter(this@AttendanceActivity, android.R.layout.simple_spinner_dropdown_item, groups)
            spGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentGroup = groups[position]
                    loadLessons()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun loadLessons() {
        lifecycleScope.launch {
            val lessons = db.lessonDao().getForGroup(currentGroup)
            if (lessons.isEmpty()) {
                spLesson.adapter = null
                currentLesson = null
                currentStudents = emptyList()
                adapter.submit(emptyList(), emptyMap())
                updateStatistics()
                btnDeleteLesson.isEnabled = false
                btnSave.isEnabled = false
                return@launch
            }

            btnDeleteLesson.isEnabled = true
            btnSave.isEnabled = true

            val lessonTitles = lessons.map { "${it.date} - ${it.subject}" }
            spLesson.adapter = ArrayAdapter(this@AttendanceActivity, android.R.layout.simple_spinner_dropdown_item, lessonTitles)
            spLesson.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentLesson = lessons[position]
                    loadSavedMarks()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun showAddLessonDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_lesson, null)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val etSubject = dialogView.findViewById<EditText>(R.id.etSubject)
        etDate.setText(DateTools.today())

        AlertDialog.Builder(this)
            .setTitle("Новое занятие для $currentGroup")
            .setView(dialogView)
            .setPositiveButton("Создать") { _, _ ->
                val date = etDate.text.toString().trim()
                val subject = etSubject.text.toString().trim()
                if (!DateTools.isValidIsoDate(date)) {
                    Toast.makeText(this, "Неверный формат даты", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (subject.isEmpty()) {
                    Toast.makeText(this, "Введите тему", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    db.lessonDao().insert(LessonEntity(groupName = currentGroup, date = date, subject = subject))
                    loadLessons()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteCurrentLesson() {
        val lesson = currentLesson ?: return
        AlertDialog.Builder(this)
            .setTitle("Удалить занятие?")
            .setMessage("Занятие от ${lesson.date} (${lesson.subject}) будет удалено. Все отметки тоже исчезнут.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    db.lessonDao().delete(lesson.groupName, lesson.date, lesson.subject)
                    db.attendanceDao().deleteForLesson(lesson.date, lesson.subject)
                    loadLessons()
                    Toast.makeText(this@AttendanceActivity, "Занятие удалено", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun loadSavedMarks() {
        val lesson = currentLesson ?: return
        lifecycleScope.launch {
            currentStudents = db.studentDao().getByGroup(lesson.groupName)
            val marks = db.attendanceDao().getForLesson(lesson.date, lesson.subject)
            val savedStatuses = mutableMapOf<Int, String>()
            marks.forEach { savedStatuses[it.studentId] = it.status }

            adapter.submit(currentStudents, savedStatuses)
            updateStatistics()
        }
    }

    private fun updateStatistics() {
        if (currentStudents.isEmpty()) {
            tvStats.text = "Нет студентов"
            return
        }
        val (present, absent, late) = adapter.getStatistics()
        tvStats.text = "Присутствовали: $present | Отсутствовали: $absent | Опоздали: $late"
    }

    private fun save() {
        val lesson = currentLesson ?: return
        val statuses = adapter.getStatuses()
        val marks = currentStudents.mapNotNull { student ->
            statuses[student.id]?.let { status ->
                AttendanceEntity(
                    studentId = student.id,
                    lessonDate = lesson.date,
                    subject = lesson.subject,
                    status = status
                )
            }
        }
        lifecycleScope.launch {
            db.attendanceDao().upsertAll(marks)
            Toast.makeText(this@AttendanceActivity, "Отметки сохранены: ${marks.size}", Toast.LENGTH_SHORT).show()
        }
    }
}