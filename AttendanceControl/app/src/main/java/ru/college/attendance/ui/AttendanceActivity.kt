package ru.college.attendance.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.college.attendance.R
import ru.college.attendance.data.*
import ru.college.attendance.logic.DateTools

class AttendanceActivity : AppCompatActivity() {

    private val adapter by lazy { AttendanceAdapter { updateStatistics() } }

    private var currentGroup: String = ""
    private var currentLesson: Lesson? = null
    private var currentStudents: List<Student> = emptyList()

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
        val groups = StudentRepository.getAll().map { it.groupName }.distinct().sorted()
        if (groups.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            spGroup.visibility = View.GONE
            btnAddLesson.isEnabled = false
            btnDeleteLesson.isEnabled = false
            btnSave.isEnabled = false
            return
        }

        spGroup.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, groups)
        spGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentGroup = groups[position]
                loadLessons() // При смене группы загружаем её занятия
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadLessons() {
        val lessons = LessonRepository.getForGroup(currentGroup)

        // Если занятий нет, очищаем список, отключаем кнопку удаления и показываем заглушку
        if (lessons.isEmpty()) {
            spLesson.adapter = null
            currentLesson = null
            currentStudents = emptyList()
            adapter.submit(emptyList(), emptyMap())
            updateStatistics()

            btnDeleteLesson.isEnabled = false // Отключаем кнопку
            btnSave.isEnabled = false // Отключаем сохранение, если нет занятия
            return
        }

        // Если занятия есть, включаем кнопки
        btnDeleteLesson.isEnabled = true
        btnSave.isEnabled = true

        val lessonTitles = lessons.map { "${it.date} - ${it.subject}" }
        spLesson.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lessonTitles)

        spLesson.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentLesson = lessons[position]
                loadSavedMarks()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
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

                // Сохраняем занятие в базу
                LessonRepository.add(currentGroup, date, subject)
                loadLessons() // Обновляем выпадающий список занятий
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteCurrentLesson() {
        val lesson = currentLesson
        if (lesson == null) {
            Toast.makeText(this, "Сначала выберите занятие", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Удалить занятие?")
            .setMessage("Занятие от ${lesson.date} (${lesson.subject}) будет удалено. Все отметки посещаемости для него тоже исчезнут.")
            .setPositiveButton("Удалить") { _, _ ->
                // 1. Удаляем само занятие
                LessonRepository.remove(lesson)

                // 2. Удаляем все отметки, привязанные к этому занятию
                AttendanceRepository.removeMarksForLesson(lesson.date, lesson.subject)

                // 3. Обновляем список занятий на экране
                loadLessons()
                Toast.makeText(this, "Занятие удалено", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun loadSavedMarks() {
        val lesson = currentLesson ?: return
        currentStudents = StudentRepository.getAll().filter { it.groupName == lesson.groupName }

        val savedStatuses = mutableMapOf<Int, String>()
        val marks = AttendanceRepository.getMarksForLesson(lesson.date, lesson.subject)
        marks.forEach { mark -> savedStatuses[mark.studentId] = mark.status }

        adapter.submit(currentStudents, savedStatuses)
        updateStatistics()
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
        val lesson = currentLesson
        if (lesson == null) {
            Toast.makeText(this, "Сначала создайте занятие!", Toast.LENGTH_SHORT).show()
            return
        }

        val statuses = adapter.getStatuses()
        currentStudents.forEach { student ->
            val status = statuses[student.id] ?: AttendanceStatus.PRESENT
            AttendanceRepository.saveMark(student.id, lesson.date, lesson.subject, status)
        }
        Toast.makeText(this, "Отметки для занятия сохранены!", Toast.LENGTH_SHORT).show()
    }
}