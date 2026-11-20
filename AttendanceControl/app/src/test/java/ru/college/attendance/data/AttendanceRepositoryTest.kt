package ru.college.attendance.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AttendanceRepositoryTest {

    @Before
    fun setUp() {
        AttendanceRepository.clear()
    }

    @Test
    fun saveMark_storesStatus() {
        AttendanceRepository.saveMark(1, "2026-09-01", "Программирование", AttendanceStatus.ABSENT)
        val mark = AttendanceRepository.find(1, "2026-09-01", "Программирование")
        assertNotNull(mark)
        assertEquals(AttendanceStatus.ABSENT, mark!!.status)
    }

    @Test
    fun repeatedMark_replacesPreviousOne() {
        AttendanceRepository.saveMark(1, "2026-09-01", "Программирование", AttendanceStatus.ABSENT)
        AttendanceRepository.saveMark(1, "2026-09-01", "программирование", AttendanceStatus.LATE)
        assertEquals(1, AttendanceRepository.getAll().size)
        assertEquals(AttendanceStatus.LATE, AttendanceRepository.getAll().first().status)
    }

    @Test
    fun unknownMark_returnsNull() {
        assertNull(AttendanceRepository.find(5, "2026-09-01", "Базы данных"))
    }
}
