package ru.college.attendance.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(lesson: LessonEntity): Long

    @Query("SELECT * FROM lessons WHERE groupName = :group ORDER BY date DESC")
    suspend fun getForGroup(group: String): List<LessonEntity>

    @Query("DELETE FROM lessons WHERE groupName = :group AND date = :date AND subject = :subject")
    suspend fun delete(group: String, date: String, subject: String)
}