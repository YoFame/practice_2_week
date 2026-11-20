package ru.college.attendance.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    indices = [Index(value = ["groupName", "date", "subject"], unique = true)]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val date: String,
    val subject: String
)