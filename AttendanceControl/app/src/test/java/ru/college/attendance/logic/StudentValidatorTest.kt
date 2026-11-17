package ru.college.attendance.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudentValidatorTest {

    @Test
    fun validData_returnsNull() {
        assertNull(StudentValidator.validate("Иванов", "Пётр", "ИС-21"))
    }

    @Test
    fun emptyFields_returnError() {
        assertEquals(StudentValidator.MSG_EMPTY, StudentValidator.validate("", "Пётр", "ИС-21"))
        assertEquals(StudentValidator.MSG_EMPTY, StudentValidator.validate("Иванов", "", "ИС-21"))
        assertEquals(StudentValidator.MSG_EMPTY, StudentValidator.validate("Иванов", "Пётр", ""))
    }

    @Test
    fun onlySpaces_returnError() {
        assertEquals(StudentValidator.MSG_EMPTY, StudentValidator.validate("   ", "Пётр", "ИС-21"))
    }

    @Test
    fun duplicate_ignoresCaseAndExtraSpaces() {
        val existing = listOf(StudentKey("Иванов", "Пётр", "ИС-21"))
        assertEquals(
            StudentValidator.MSG_DUPLICATE,
            StudentValidator.validate("  иванов ", "ПЁТР", "ис-21", existing)
        )
    }

    @Test
    fun sameNameInAnotherGroup_isAllowed() {
        val existing = listOf(StudentKey("Иванов", "Пётр", "ИС-21"))
        assertNull(StudentValidator.validate("Иванов", "Пётр", "ИС-22", existing))
    }

    @Test
    fun normalize_collapsesSpaces() {
        assertEquals("Иванов Пётр", StudentValidator.normalize("  Иванов    Пётр "))
    }
}
