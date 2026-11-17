package ru.college.attendance.logic

/** Ключ студента для проверки дубликатов. */
data class StudentKey(
    val lastName: String,
    val firstName: String,
    val groupName: String
)

object StudentValidator {
    const val MSG_EMPTY = "Заполните все поля: фамилия, имя и группа"
    const val MSG_DUPLICATE = "Такой студент уже есть в этой группе"

    /** Убирает пробелы по краям и сжимает повторяющиеся пробелы. */
    fun normalize(text: String): String = text.trim().replace(Regex("\\s+"), " ")

    /** Возвращает текст ошибки или null, если данные корректны. */
    fun validate(
        lastName: String,
        firstName: String,
        groupName: String,
        existing: List<StudentKey> = emptyList()
    ): String? {
        val last = normalize(lastName)
        val first = normalize(firstName)
        val group = normalize(groupName)
        if (last.isEmpty() || first.isEmpty() || group.isEmpty()) return MSG_EMPTY

        val key = StudentKey(last.lowercase(), first.lowercase(), group.lowercase())
        val duplicate = existing.any {
            StudentKey(
                normalize(it.lastName).lowercase(),
                normalize(it.firstName).lowercase(),
                normalize(it.groupName).lowercase()
            ) == key
        }
        return if (duplicate) MSG_DUPLICATE else null
    }
}
