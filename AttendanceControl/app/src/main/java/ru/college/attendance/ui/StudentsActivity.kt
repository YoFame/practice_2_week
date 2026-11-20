package ru.college.attendance.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.college.attendance.R
import ru.college.attendance.data.AppDatabase
import ru.college.attendance.data.StudentEntity
import ru.college.attendance.logic.StudentKey
import ru.college.attendance.logic.StudentValidator

class StudentsActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.get(this) }
    private lateinit var adapter: StudentAdapter
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)

        val etLast = findViewById<EditText>(R.id.etLastName)
        val etFirst = findViewById<EditText>(R.id.etFirstName)
        val etGroup = findViewById<EditText>(R.id.etGroup)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = StudentAdapter { student -> confirmDelete(student) }
        findViewById<RecyclerView>(R.id.rvStudents).apply {
            layoutManager = LinearLayoutManager(this@StudentsActivity)
            adapter = this@StudentsActivity.adapter
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            lifecycleScope.launch {
                val existing = db.studentDao().getAll()
                    .map { StudentKey(it.lastName, it.firstName, it.groupName) }
                val error = StudentValidator.validate(
                    etLast.text.toString(), etFirst.text.toString(), etGroup.text.toString(), existing
                )
                if (error != null) {
                    Toast.makeText(this@StudentsActivity, error, Toast.LENGTH_LONG).show()
                } else {
                    db.studentDao().insert(
                        StudentEntity(
                            lastName = StudentValidator.normalize(etLast.text.toString()),
                            firstName = StudentValidator.normalize(etFirst.text.toString()),
                            groupName = StudentValidator.normalize(etGroup.text.toString())
                        )
                    )
                    etLast.text.clear()
                    etFirst.text.clear()
                    etGroup.text.clear()
                    Toast.makeText(this@StudentsActivity, "Студент добавлен", Toast.LENGTH_SHORT).show()
                    refresh()
                }
            }
        }
        refresh()
    }

    private fun confirmDelete(student: StudentEntity) {
        AlertDialog.Builder(this)
            .setTitle("Удалить студента?")
            .setMessage("${student.lastName} ${student.firstName} (${student.groupName}). " +
                    "Его отметки посещаемости тоже будут удалены.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    db.studentDao().delete(student)
                    refresh()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun refresh() {
        lifecycleScope.launch {
            val list = db.studentDao().getAll()
            adapter.submit(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}