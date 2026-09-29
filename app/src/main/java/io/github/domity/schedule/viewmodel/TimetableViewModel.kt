package io.github.domity.schedule.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.domity.schedule.data.CourseDatabase
import io.github.domity.schedule.data.CourseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val Application.dataStore by preferencesDataStore(name = "settings")

private val DEFAULT_LESSON_TIMES = listOf(
    "08:00\n08:45", "08:50\n09:35", "09:50\n10:35", "10:45\n11:30",
    "11:35\n12:20", "14:00\n14:45", "14:50\n15:35", "15:55\n16:40",
    "16:45\n17:30", "18:30\n19:15", "19:20\n20:05", "20:10\n20:55"
)

// 启动关键设置的同步镜像（SharedPreferences 支持同步读取，DataStore 不支持）。
private const val SYNC_PREFS = "startup_prefs"
private const val SYNC_SHOW_WEEKEND = "show_weekend"
private const val SYNC_LESSON_TIMES = "lesson_times"
private const val SYNC_MIGRATED = "migrated_from_datastore"

class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val app: Application = application
    private val database = CourseDatabase.getDatabase(application)
    private val courseDao = database.courseDao()
    
    val courses: StateFlow<List<CourseEntity>> = courseDao.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val SEMESTER_START_KEY = stringPreferencesKey("semester_start_date")
    private val SHOW_WEEKEND_KEY = booleanPreferencesKey("show_weekend")
    private val LESSON_TIMES_KEY = stringPreferencesKey("lesson_times")

    // Single shared DataStore flow. Exposing stable StateFlows avoids rebuilding the
    // whole preference pipeline on every recomposition (cold-start + scroll cost).
    private val preferences = app.dataStore.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyPreferences())

    // 同步读取的启动镜像：ViewModel 构造发生在首帧绘制之前，
    // 因此首帧就能拿到正确的布局相关设置，避免"先画周末再隐藏"的闪烁。
    private val syncPrefs = app.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)

    private val _showWeekend = MutableStateFlow(syncPrefs.getBoolean(SYNC_SHOW_WEEKEND, true))
    private val _lessonTimes = MutableStateFlow(
        syncPrefs.getString(SYNC_LESSON_TIMES, null)?.split("|") ?: DEFAULT_LESSON_TIMES
    )

    /** 是否显示周末 (首帧即为正确值) */
    val showWeekend: StateFlow<Boolean> = _showWeekend.asStateFlow()

    /** 课程时间 (首帧即为正确值)，元素格式 "HH:MM\nHH:MM" */
    val lessonTimes: StateFlow<List<String>> = _lessonTimes.asStateFlow()

    init {
        if (!syncPrefs.getBoolean(SYNC_MIGRATED, false)) {
            viewModelScope.launch {
                val prefs = preferences.first()
                val dsWeekend = prefs[SHOW_WEEKEND_KEY]
                val dsTimes = prefs[LESSON_TIMES_KEY]
                syncPrefs.edit {
                    dsWeekend?.let {
                        putBoolean(SYNC_SHOW_WEEKEND, it)
                        _showWeekend.value = it
                    }
                    dsTimes?.let {
                        putString(SYNC_LESSON_TIMES, it)
                        _lessonTimes.value = it.split("|")
                    }
                    putBoolean(SYNC_MIGRATED, true)
                }
            }
        }
    }

    /**
     * 切换是否显示周末
     */
    fun toggleShowWeekend() {
        val newValue = !_showWeekend.value
        _showWeekend.value = newValue
        // 同步写入镜像
        syncPrefs.edit { putBoolean(SYNC_SHOW_WEEKEND, newValue) }
        // 保留 DataStore 写入以维持一致性
        viewModelScope.launch {
            app.dataStore.edit { it[SHOW_WEEKEND_KEY] = newValue }
        }
    }
    
    /**
     * 更新课程时间
     */
    fun updateLessonTime(index: Int, startTime: String, endTime: String) {
        val times = _lessonTimes.value.toMutableList()
        if (index in times.indices) {
            times[index] = "$startTime\n$endTime"
            _lessonTimes.value = times
            val joined = times.joinToString("|")
            syncPrefs.edit { putString(SYNC_LESSON_TIMES, joined) }
            viewModelScope.launch {
                app.dataStore.edit { it[LESSON_TIMES_KEY] = joined }
            }
        }
    }

    // 当前周数
    val currentWeek: StateFlow<Int> = preferences
        .map { prefs ->
            val startDateStr = prefs[SEMESTER_START_KEY] ?: return@map 1
            try {
                val startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"))
                val daysBetween = ChronoUnit.DAYS.between(startDate, LocalDate.now())
                ((daysBetween / 7).toInt() + 1).coerceAtLeast(1)
            } catch (_: Exception) {
                1
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

// 设置学期开始日期以计算周数
    fun setSemesterStartDate(date: String) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[SEMESTER_START_KEY] = date
            }
        }
    }
    
// 添加一个课程
    fun addCourse(course: CourseEntity) {
        viewModelScope.launch {
            courseDao.insertCourse(course)
        }
    }
    
// 更新一个课程
    fun updateCourse(course: CourseEntity) {
        viewModelScope.launch {
            courseDao.updateCourse(course)
        }
    }
    
// 删除一个课程
    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            courseDao.deleteCourse(course)
        }
    }

// 合并一个课程
    fun increaseSpan(course: CourseEntity) {
        viewModelScope.launch {
            // 从数据库获取最新的课程数据
            val latestCourse = courseDao.getCourseById(course.id) ?: return@launch
            // 检查边界
            if (latestCourse.startLesson + latestCourse.span > 12) {
                return@launch
            }
            // 检查是否会与其他课程冲突
            val conflict = hasCourseConflict(
                dayOfWeek = latestCourse.dayOfWeek,
                startLesson = latestCourse.startLesson,
                span = latestCourse.span + 1,
                excludeCourseId = latestCourse.id
            )
            if (!conflict) {
                val updated = latestCourse.copy(span = latestCourse.span + 1)
                updateCourse(updated)
            }
        }
    }
    
// 拆分一个课程
    fun decreaseSpan(course: CourseEntity) {
        viewModelScope.launch {
            // 依旧从数据库获取最新的课程数据
            val latestCourse = courseDao.getCourseById(course.id) ?: return@launch
            if (latestCourse.span > 1) {
                val updated = latestCourse.copy(span = latestCourse.span - 1)
                updateCourse(updated)
            }
        }
    }
    
// 更新课程颜色
    fun updateCourseColor(course: CourseEntity, newColorHex: String) {
        val updated = course.copy(colorHex = newColorHex)
        updateCourse(updated)
    }
    
// 检查是否冲突？
    suspend fun hasCourseConflict(
        dayOfWeek: Int,
        startLesson: Int,
        span: Int,
        excludeCourseId: String? = null
    ): Boolean {
        val coursesOnDay = courseDao.getCoursesByDay(dayOfWeek)
        val endLesson = startLesson + span - 1
        
        return coursesOnDay.any { existing ->
            if (excludeCourseId != null && existing.id == excludeCourseId) {
                false
            } else {
                val existingEnd = existing.startLesson + existing.span - 1
                !(endLesson < existing.startLesson || startLesson > existingEnd)
            }
        }
    }
}
