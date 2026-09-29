package io.github.domity.schedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val teacher: String,
    val dayOfWeek: Int, // 0-6 周一到周五
    val startLesson: Int, // 1-12
    val span: Int,
    val colorHex: String // e.g. "#FF5733"
)
