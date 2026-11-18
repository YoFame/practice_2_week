package ru.college.attendance.data

object AttendanceStatus {
    const val PRESENT = "Присутствовал"
    const val ABSENT = "Отсутствовал"
    const val LATE = "Опоздал"

    val ALL = listOf(PRESENT, ABSENT, LATE)
}
