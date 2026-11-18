package ru.college.attendance.data

// Модель самого занятия (без студентов)
data class Lesson(
    val id: Int,
    val groupName: String,
    val date: String,    // Формат ГГГГ-ММ-ДД
    val subject: String  // Тема / дисциплина
)