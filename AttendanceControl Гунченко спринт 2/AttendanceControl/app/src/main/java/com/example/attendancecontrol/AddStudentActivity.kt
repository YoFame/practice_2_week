package com.example.attendancecontrol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddStudentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etGroup = findViewById<EditText>(R.id.etGroup)

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val group = etGroup.text.toString().trim()

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent().apply {
                putExtra("fullName", fullName)
                putExtra("group", group)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }
    }
}