package io.github.domity.schedule.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY dayOfWeek, startLesson")
    fun getAllCourses(): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: String): CourseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)
    
    @Update
    suspend fun updateCourse(course: CourseEntity)
    
    @Delete
    suspend fun deleteCourse(course: CourseEntity)
    
    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: String)
    
    @Query("SELECT * FROM courses WHERE dayOfWeek = :day")
    suspend fun getCoursesByDay(day: Int): List<CourseEntity>
}
