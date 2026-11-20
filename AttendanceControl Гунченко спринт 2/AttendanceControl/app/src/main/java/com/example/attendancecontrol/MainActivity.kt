package com.example.attendancecontrol

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val students = mutableListOf<Student>()
    private lateinit var adapter: StudentAdapter
    private var nextId: Long = 1

    private val addStudentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val fullName = data.getStringExtra("fullName") ?: return@registerForActivityResult
            val group = data.getStringExtra("group") ?: ""
            students.add(Student(nextId++, fullName, group))
            adapter.notifyItemInserted(students.size - 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = StudentAdapter(students) { student -> confirmDelete(student) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<android.widget.Button>(R.id.btnAdd).setOnClickListener {
            addStudentLauncher.launch(Intent(this, AddStudentActivity::class.java))
        }
    }

    private fun confirmDelete(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Удалить студента?")
            .setMessage("${student.fullName} будет удалён из списка.")
            .setPositiveButton("Удалить") { _, _ ->
                val index = students.indexOf(student)
                if (index >= 0) {
                    students.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}