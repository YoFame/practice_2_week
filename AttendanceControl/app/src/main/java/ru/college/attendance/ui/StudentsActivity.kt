package ru.college.attendance.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.college.attendance.R
import ru.college.attendance.data.Student
import ru.college.attendance.data.StudentRepository
import ru.college.attendance.logic.StudentKey
import ru.college.attendance.logic.StudentValidator

class StudentsActivity : AppCompatActivity() {

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
            val existing = StudentRepository.getAll()
                .map { StudentKey(it.lastName, it.firstName, it.groupName) }
            val error = StudentValidator.validate(
                etLast.text.toString(), etFirst.text.toString(), etGroup.text.toString(), existing
            )
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            } else {
                StudentRepository.add(
                    StudentValidator.normalize(etLast.text.toString()),
                    StudentValidator.normalize(etFirst.text.toString()),
                    StudentValidator.normalize(etGroup.text.toString())
                )
                etLast.text.clear()
                etFirst.text.clear()
                Toast.makeText(this, "Студент добавлен", Toast.LENGTH_SHORT).show()
                refresh()
            }
        }
        refresh()
    }

    private fun confirmDelete(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Удалить студента?")
            .setMessage("${student.lastName} ${student.firstName} (${student.groupName})")
            .setPositiveButton("Удалить") { _, _ ->
                StudentRepository.remove(student.id)
                refresh()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun refresh() {
        val list = StudentRepository.getAll()
        adapter.submit(list)
        tvEmpty.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}
