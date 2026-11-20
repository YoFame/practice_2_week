package ru.college.attendance.logic

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Инструменты для работы с датами.
 * Даты храним строкой ГГГГ-ММ-ДД: так их легко сравнивать и сортировать.
 */
object DateTools {
    private const val PATTERN = "yyyy-MM-dd"

    // Возвращает сегодняшнюю дату в нужном формате
    fun today(): String = SimpleDateFormat(PATTERN, Locale.US).format(Date())

    // Проверяет, ввел ли пользователь реальную дату в формате ГГГГ-ММ-ДД
    fun isValidIsoDate(text: String): Boolean {
        if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(text)) return false
        val format = SimpleDateFormat(PATTERN, Locale.US)
        format.isLenient = false // Запрещаем вводить несуществующие даты (например, 31 февраля)
        return try {
            format.parse(text) != null
        } catch (e: ParseException) {
            false
        }
    }
}