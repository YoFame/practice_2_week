package ru.college.attendance.data

/** Хранилище занятий в памяти (для 3-го спринта). */
object LessonRepository {
    private val lessons = mutableListOf<Lesson>()
    private var nextId = 1

    fun add(groupName: String, date: String, subject: String): Lesson {
        // Проверяем, нет ли уже такого занятия (та же группа, дата и тема)
        val existing = lessons.find {
            it.groupName == groupName && it.date == date && it.subject.equals(subject, true)
        }
        if (existing != null) return existing // Если есть, возвращаем его

        val lesson = Lesson(nextId++, groupName, date, subject)
        lessons.add(lesson)
        return lesson
    }

    // Получить все занятия для конкретной группы
    fun getForGroup(groupName: String): List<Lesson> =
        lessons.filter { it.groupName == groupName }.sortedByDescending { it.date }

    // Удалить занятие
    fun remove(lesson: Lesson) {
        lessons.remove(lesson)
    }

    fun clear() {
        lessons.clear()
        nextId = 1
    }
}