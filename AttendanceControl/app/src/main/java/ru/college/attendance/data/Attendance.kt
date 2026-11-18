package ru.college.attendance.data

data class Attendance(
    val id: Int,
    val studentId: Int,
    val lessonDate: String,
    val subject: String,
    val status: String
)
