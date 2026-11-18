package ru.college.attendance.logic

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Даты хранятся строкой ГГГГ-ММ-ДД: так их можно сравнивать как строки. */
object DateTools {
    private const val PATTERN = "yyyy-MM-dd"

    fun today(): String = SimpleDateFormat(PATTERN, Locale.US).format(Date())

    fun isValidIsoDate(text: String): Boolean {
        if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(text)) return false
        val format = SimpleDateFormat(PATTERN, Locale.US)
        format.isLenient = false
        return try {
            format.parse(text) != null
        } catch (e: ParseException) {
            false
        }
    }
}
