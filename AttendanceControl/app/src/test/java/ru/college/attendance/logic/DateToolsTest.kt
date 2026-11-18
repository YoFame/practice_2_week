package ru.college.attendance.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateToolsTest {

    @Test
    fun validDate_isAccepted() {
        assertTrue(DateTools.isValidIsoDate("2026-09-01"))
    }

    @Test
    fun wrongFormat_isRejected() {
        assertFalse(DateTools.isValidIsoDate("01.09.2026"))
        assertFalse(DateTools.isValidIsoDate(""))
    }

    @Test
    fun nonexistentDate_isRejected() {
        assertFalse(DateTools.isValidIsoDate("2026-02-30"))
        assertFalse(DateTools.isValidIsoDate("2026-13-01"))
    }

    @Test
    fun today_isValid() {
        assertTrue(DateTools.isValidIsoDate(DateTools.today()))
    }
}
