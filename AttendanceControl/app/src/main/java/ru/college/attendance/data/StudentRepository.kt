package ru.college.attendance.data

/** Временное хранилище в памяти (спринты 2-3). В спринте 4 заменено на Room. */
object StudentRepository {
    private val students = mutableListOf<Student>()
    private var nextId = 1

    fun getAll(): List<Student> =
        students.sortedWith(compareBy({ it.groupName }, { it.lastName }, { it.firstName }))

    fun add(lastName: String, firstName: String, groupName: String): Student {
        val student = Student(nextId++, firstName, lastName, groupName)
        students.add(student)
        return student
    }

    fun remove(id: Int) {
        students.removeAll { it.id == id }
    }

    fun clear() {
        students.clear()
        nextId = 1
    }
}
