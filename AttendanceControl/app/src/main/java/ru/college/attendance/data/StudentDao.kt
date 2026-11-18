package ru.college.attendance.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(student: StudentEntity): Long

    @Query("SELECT * FROM students ORDER BY groupName, lastName, firstName")
    suspend fun getAll(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE groupName = :group ORDER BY lastName, firstName")
    suspend fun getByGroup(group: String): List<StudentEntity>

    @Query("SELECT DISTINCT groupName FROM students ORDER BY groupName")
    suspend fun getGroups(): List<String>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun count(): Int

    @Delete
    suspend fun delete(student: StudentEntity)
}
