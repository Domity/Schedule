package io.github.domity.schedule.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.domity.cybertheme.atoms.CyberText
import io.github.domity.cybertheme.atoms.CyberSurface
import io.github.domity.cybertheme.foundation.CyberTheme
import io.github.domity.cybertheme.molecules.CyberAlertDialog
import io.github.domity.cybertheme.molecules.CyberButton
import io.github.domity.cybertheme.molecules.CyberDialog
import io.github.domity.cybertheme.molecules.CyberInput
import io.github.domity.cybertheme.molecules.CyberTextButton
import io.github.domity.cybertheme.molecules.CyberTextField
import io.github.domity.schedule.data.CourseEntity
import io.github.domity.schedule.utils.ColorUtils
import io.github.domity.schedule.viewmodel.TimetableViewModel
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
// 长文件警告
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TimetableScreen(viewModel: TimetableViewModel = viewModel()) {
    val courses by viewModel.courses.collectAsState()
    val currentWeek by viewModel.currentWeek.collectAsState()
    val showWeekend by viewModel.showWeekend.collectAsState()
    val lessonTimes by viewModel.lessonTimes.collectAsState()
    
    var selectedCourseId by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var longPressMenuState by remember { mutableStateOf<LongPressMenuState?>(null) }
    var showEditDialog by remember { mutableStateOf<CourseEntity?>(null) }
    var showColorPicker by remember { mutableStateOf<CourseEntity?>(null) }
    var showAddDialog by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<CourseEntity?>(null) }
    var showEditTime by remember { mutableStateOf<Int?>(null) } // 编辑的课时索引
    
    // 根据ID从courses中获取最新的课程数据
    val selectedCourse = remember(courses, selectedCourseId) {
        selectedCourseId?.let { id -> courses.find { it.id == id } }
    }
    
    val haptic = LocalHapticFeedback.current
    
    CyberTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberTheme.colors.background)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
        AnimatedVisibility(visible = selectedCourse != null) {
            selectedCourse?.let { course ->
                CourseDetailBar(
                    course = course,
                    onDismiss = { selectedCourseId = null }
                )
            }
        }
        
        // Main timetable grid
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val sidebarWidth = 36.dp
            val headerHeight = 32.dp
            
            // 根据showWeekend状态动态调整列数
            val numDays = if (showWeekend) 7 else 5
            
            val availableWidth = maxWidth - sidebarWidth
            val availableHeight = maxHeight - headerHeight
            
            val cellWidth = availableWidth / numDays
            val cellHeight = availableHeight / 12
            
            val gridColor = CyberTheme.colors.border
            val textColor = CyberTheme.colors.text
//            val textDimColor = CyberTheme.colors.textDim
            val primaryColor = CyberTheme.colors.primary
            
            Column {
                // Header row with days of week
                Row(modifier = Modifier.height(headerHeight)) {
                    // 左上方周数指示器
                    Box(
                        modifier = Modifier
                            .size(sidebarWidth, headerHeight)
                            .border(0.5.dp, gridColor)
                            .clickable { showDatePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        CyberText(
                            text = "W $currentWeek",
                            style = CyberTheme.typography.body.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = primaryColor
                        )
                    }
                    
                    // 天数
                    val daysOfWeek = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                    val displayDays = if (showWeekend) daysOfWeek else daysOfWeek.take(5)
                    displayDays.forEachIndexed { _, day ->
                        Box(
                            modifier = Modifier
                                .size(cellWidth, headerHeight)
                                .border(0.5.dp, gridColor)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            // 只在周五时响应长按
                                            if (day == "周五") {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.toggleShowWeekend()
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CyberText(
                                text = day,
                                style = CyberTheme.typography.body.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = textColor
                            )
                        }
                    }
                }
                
                // Grid area
                Row {
                    // 左侧时间表
                    Column(modifier = Modifier.width(sidebarWidth)) {
                        lessonTimes.forEachIndexed { index, time ->
                            Box(
                                modifier = Modifier
                                    .size(sidebarWidth, cellHeight)
                                    .border(0.5.dp, gridColor)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showEditTime = index
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CyberText(
                                        text = "${index + 1}",
                                        style = CyberTheme.typography.body.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = textColor
                                    )
                                    CyberText(
                                        text = time,
                                        style = CyberTheme.typography.body.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 8.sp
                                        ),
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                    
                    // Grid with courses
                    Box(
                        modifier = Modifier
                            .size(availableWidth, availableHeight)
                    ) {
                        // Draw background grid
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // 垂直线 根据numDays动态调整
                            for (i in 0..numDays) {
                                val x = cellWidth.toPx() * i
                                drawLine(
                                    color = gridColor,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            // 直接绘制所有线，课程砖块的黑色背景会覆盖，省的复杂判断
                            for (i in 0..12) {
                                val y = cellHeight.toPx() * i
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                        
                        // 空格点击handlers
                        for (day in 0 until numDays) {
                            for (lesson in 1..12) {
                                val hasCourse = courses.any { course ->
                                    course.dayOfWeek == day &&
                                    lesson >= course.startLesson &&
                                    lesson < course.startLesson + course.span
                                }
                                
                                if (!hasCourse) {
                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = cellWidth * day,
                                                y = cellHeight * (lesson - 1)
                                            )
                                            .size(cellWidth, cellHeight)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = {
                                                        // 点击空白格子关闭详情窗口
                                                        selectedCourseId = null
                                                    },
                                                    onLongPress = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        longPressMenuState = LongPressMenuState.EmptyCell(day, lesson, it)
                                                    }
                                                )
                                            }
                                    )
                                }
                            }
                        }
                        
                        // 渲染当前显示天数范围内的课程
                        courses.filter { it.dayOfWeek < numDays }.forEach { course ->
                            CourseCard(
                                course = course,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                isSelected = selectedCourse?.id == course.id,
                                onClick = {
                                    selectedCourseId = if (selectedCourse?.id == course.id) null else course.id
                                },
                                onLongPress = { offset ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    longPressMenuState = LongPressMenuState.Course(course, offset)
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // 长按菜单
        longPressMenuState?.let { menuState ->
            when (menuState) {
                is LongPressMenuState.Course -> {
                    // 从最新的courses列表中获取课程数据，避免使用过时对象
                    val currentCourse = courses.find { it.id == menuState.course.id } ?: menuState.course
                    
                    CourseContextMenu(
                        course = currentCourse,
                        onDismiss = { longPressMenuState = null },
                        onEdit = {
                            showEditDialog = currentCourse
                            longPressMenuState = null
                        },
                        onChangeColor = {
                            showColorPicker = currentCourse
                            longPressMenuState = null
                        },
                        onIncreaseSpan = {
                            viewModel.increaseSpan(currentCourse)
                            longPressMenuState = null
                        },
                        onDecreaseSpan = {
                            viewModel.decreaseSpan(currentCourse)
                            longPressMenuState = null
                        },
                        onDelete = {
                            showDeleteConfirm = currentCourse
                            longPressMenuState = null
                        }
                    )
                }
                is LongPressMenuState.EmptyCell -> {
                    EmptyCellMenu(
                        dayOfWeek = menuState.dayOfWeek,
                        startLesson = menuState.startLesson,
                        onDismiss = { longPressMenuState = null },
                        onAddCourse = { day, lesson ->
                            showAddDialog = Pair(day, lesson)
                            longPressMenuState = null
                        }
                    )
                }
            }
        }
        
        // 日期选择弹窗
        if (showDatePicker) {
            DatePickerDialog(
                onDismiss = { showDatePicker = false },
                onDateSelected = { dateString ->
                    viewModel.setSemesterStartDate(dateString)
                    showDatePicker = false
                }
            )
        }
        
        // 编辑课程弹窗
        showEditDialog?.let { course ->
            EditCourseDialog(
                course = course,
                viewModel = viewModel,
                onDismiss = { showEditDialog = null }
            )
        }
        
        // 颜色选择弹窗
        showColorPicker?.let { course ->
            ColorPickerDialog(
                course = course,
                viewModel = viewModel,
                onDismiss = { showColorPicker = null }
            )
        }
        
        // 增加课程弹窗
        showAddDialog?.let { (day, lesson) ->
            AddCourseDialog(
                dayOfWeek = day,
                startLesson = lesson,
                viewModel = viewModel,
                onDismiss = { showAddDialog = null }
            )
        }
        
        // 删除确认弹窗
        showDeleteConfirm?.let { course ->
            DeleteConfirmDialog(
                course = course,
                onConfirm = {
                    viewModel.deleteCourse(course)
                    showDeleteConfirm = null
                },
                onDismiss = { showDeleteConfirm = null }
            )
        }
        
        // 编辑时间弹窗
        showEditTime?.let { index ->
            if (index in lessonTimes.indices) {
                EditTimeDialog(
                    lessonIndex = index,
                    currentTime = lessonTimes[index],
                    viewModel = viewModel,
                    onDismiss = { showEditTime = null }
                )
            }
        }
        }
    }
}

@Composable
fun CourseCard(
    course: CourseEntity,
    cellWidth: Dp,
    cellHeight: Dp,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: (Offset) -> Unit
) {
    val primaryColor = ColorUtils.parseColor(course.colorHex)
    val backgroundColor = ColorUtils.getTransparentColor(primaryColor, 0.20f)
    
    Box(
        modifier = Modifier
            .offset(
                x = cellWidth * course.dayOfWeek,
                y = cellHeight * (course.startLesson - 1)
            )
            .size(cellWidth, cellHeight * course.span)
    ) {
        // 最下层：覆盖分割线的黑色背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000), RoundedCornerShape(2.dp))
        )
        
        // 第二层：课程内容和边框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = primaryColor,
                    shape = RoundedCornerShape(2.dp)
                )
                .background(backgroundColor, RoundedCornerShape(2.dp))
                .pointerInput(course.id, isSelected) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { offset -> onLongPress(offset) }
                    )
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CyberText(
                    text = course.name,
                    style = CyberTheme.typography.body.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
                if (course.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    CyberText(
                        text = course.location,
                        style = CyberTheme.typography.body.copy(
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        ),
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun CourseDetailBar(course: CourseEntity, onDismiss: () -> Unit) {
    // 动态构建文本内容，只包含非空字段
    val parts = mutableListOf<String>()
    if (course.name.isNotEmpty()) parts.add(course.name)
    if (course.location.isNotEmpty()) parts.add(course.location)
    if (course.teacher.isNotEmpty()) parts.add(course.teacher)
    
    val displayText = parts.joinToString(" | ")
    
    CyberSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDismiss() },
        color = CyberTheme.colors.surface,
        borderWidth = 0.5.dp,
        borderColor = CyberTheme.colors.border
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CyberText(
                text = displayText,
                style = CyberTheme.typography.body.copy(fontSize = 12.sp),
                color = CyberTheme.colors.text,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CourseContextMenu(
    course: CourseEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onChangeColor: () -> Unit,
    onIncreaseSpan: () -> Unit,
    onDecreaseSpan: () -> Unit,
    onDelete: () -> Unit
) {
    CyberAlertDialog(
        onDismissRequest = onDismiss,
        title = "课程操作",
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CyberButton(
                    text = "编辑课程",
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                )
                CyberButton(
                    text = "修改主题色",
                    onClick = onChangeColor,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                )
                CyberButton(
                    text = "向下合并一节",
                    onClick = onIncreaseSpan,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                )
                if (course.span > 1) {
                    CyberButton(
                        text = "向上拆分一节",
                        onClick = onDecreaseSpan,
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = true,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                CyberButton(
                    text = "删除课程",
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        },
        dismissButton = {
            CyberTextButton(text = "取消", onClick = onDismiss)
        }
    )
}

@Composable
fun EmptyCellMenu(
    dayOfWeek: Int,
    startLesson: Int,
    onDismiss: () -> Unit,
    onAddCourse: (Int, Int) -> Unit
) {
    CyberDialog(
        onDismissRequest = onDismiss,
        title = {
            CyberText(
                text = "添加课程",
                style = CyberTheme.typography.headline
            )
        },
        content = {
            CyberText(
                text = "在此时间段添加新课程",
                color = CyberTheme.colors.text
            )
        },
        buttons = {
            CyberButton(
                text = "取消",
                onClick = onDismiss,
                isPrimary = false
            )
            Spacer(modifier = Modifier.width(16.dp))
            CyberButton(
                text = "添加",
                onClick = { onAddCourse(dayOfWeek, startLesson) },
                isPrimary = true
            )
        }
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var yearInput by remember { mutableStateOf("") }
    var monthInput by remember { mutableStateOf("") }
    var dayInput by remember { mutableStateOf("") }
    
    CyberDialog(
        onDismissRequest = onDismiss,
        title = {
            CyberText(
                text = "设置学期开始日期",
                style = CyberTheme.typography.headline
            )
        },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CyberText(
                    text = "请输入开学第一天的日期",
                    style = CyberTheme.typography.body.copy(fontSize = 14.sp),
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = CyberTheme.colors.text
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CyberTextField(
                        value = yearInput,
                        onValueChange = { if (it.length <= 4) yearInput = it },
                        label = "年",
                        placeholder = "2024",
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    CyberTextField(
                        value = monthInput,
                        onValueChange = { if (it.length <= 2) monthInput = it },
                        label = "月",
                        placeholder = "1",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    CyberTextField(
                        value = dayInput,
                        onValueChange = { if (it.length <= 2) dayInput = it },
                        label = "日",
                        placeholder = "1",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        buttons= {
            CyberButton(
                text = "取消",
                onClick = onDismiss,
                isPrimary = false
            )
            Spacer(modifier = Modifier.width(16.dp))
            CyberButton(
                text = "确定",
                onClick = {
                    val dateString = String.format(
                        "%04d%02d%02d",
                        yearInput.toIntOrNull() ?: 2024,
                        monthInput.toIntOrNull() ?: 1,
                        dayInput.toIntOrNull() ?: 1
                    )
                    onDateSelected(dateString)
                },
                isPrimary = true
            )
        },
    )
}

sealed class LongPressMenuState {
    data class Course(val course: CourseEntity, val offset: Offset) : LongPressMenuState()
    data class EmptyCell(val dayOfWeek: Int, val startLesson: Int, val offset: Offset) : LongPressMenuState()
}

@Composable
fun EditCourseDialog(
    course: CourseEntity,
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit
) {
    var courseName by remember { mutableStateOf(course.name) }
    var location by remember { mutableStateOf(course.location) }
    var teacher by remember { mutableStateOf(course.teacher) }
    var startLesson by remember { mutableStateOf(course.startLesson.toString()) }
    var span by remember { mutableStateOf(course.span.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    CyberTheme {
        CyberDialog(
            onDismissRequest = onDismiss,
            title = {
                CyberText(
                    text = "编辑课程",
                    style = CyberTheme.typography.headline
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberInput(
                        value = courseName,
                        onValueChange = { 
                            courseName = it
                            errorMessage = null
                        },
                        placeholder = "课程名称"
                    )
                    CyberInput(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "教室地点"
                    )
                    CyberInput(
                        value = teacher,
                        onValueChange = { teacher = it },
                        placeholder = "教师姓名"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CyberInput(
                            value = startLesson,
                            onValueChange = { 
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    startLesson = it
                                    errorMessage = null
                                }
                            },
                            placeholder = "开始节次",
                            modifier = Modifier.weight(1f)
                        )
                        CyberInput(
                            value = span,
                            onValueChange = { 
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    span = it
                                    errorMessage = null
                                }
                            },
                            placeholder = "持续节数",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 显示错误信息
                    errorMessage?.let { error ->
                        CyberText(
                            text = error,
                            style = CyberTheme.typography.body.copy(fontSize = 12.sp),
                            color = CyberTheme.colors.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            buttons = {
                CyberButton(
                    text = "取消",
                    onClick = onDismiss,
                    isPrimary = false
                )
                Spacer(modifier = Modifier.width(16.dp))
                CyberButton(
                    text = "保存",
                    onClick = {
                        val newStartLesson = startLesson.toIntOrNull()?.coerceIn(1, 12) ?: course.startLesson
                        val newSpan = span.toIntOrNull()?.coerceIn(1, 12) ?: course.span
                        
                        // 检查边界
                        if (newStartLesson + newSpan > 13) {
                            errorMessage = "课程超出时间表范围"
                            return@CyberButton
                        }
                        
                        // 检查冲突
                        scope.launch {
                            val hasConflict = viewModel.hasCourseConflict(
                                dayOfWeek = course.dayOfWeek,
                                startLesson = newStartLesson,
                                span = newSpan,
                                excludeCourseId = course.id
                            )
                            
                            if (hasConflict) {
                                errorMessage = "与其他课程时间冲突"
                            } else {
                                val updatedCourse = course.copy(
                                    name = courseName.ifEmpty { course.name },
                                    location = location,
                                    teacher = teacher,
                                    startLesson = newStartLesson,
                                    span = newSpan
                                )
                                viewModel.updateCourse(updatedCourse)
                                onDismiss()
                            }
                        }
                    },
                    isPrimary = true
                )
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    course: CourseEntity,
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(course.colorHex) }
    
    SquareColorPickerDialog(
        colorHex = selectedColor,
        onColorChange = { selectedColor = it },
        onDismiss = {
            viewModel.updateCourseColor(course, selectedColor)
            onDismiss()
        },
        title = "选择主题色"
    )
}

@Composable
fun AddCourseDialog(
    dayOfWeek: Int,
    startLesson: Int,
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var span by remember { mutableStateOf("1") }
    var colorHex by remember { mutableStateOf("#FF5733") }
    var showSquarePicker by remember { mutableStateOf(false) }
    
    CyberTheme {
        // 颜色选择器弹窗（不关闭添加课程弹窗）
        if (showSquarePicker) {
            SquareColorPickerDialog(
                colorHex = colorHex,
                onColorChange = { colorHex = it },
                onDismiss = { showSquarePicker = false },
                title = "选择课程颜色"
            )
        }
        
        CyberDialog(
            onDismissRequest = onDismiss,
            title = {
                CyberText(
                    text = "添加课程",
                    style = CyberTheme.typography.headline
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberInput(
                        value = courseName,
                        onValueChange = { courseName = it },
                        placeholder = "课程名称"
                    )
                    CyberInput(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "教室地点"
                    )
                    CyberInput(
                        value = teacher,
                        onValueChange = { teacher = it },
                        placeholder = "教师姓名"
                    )
                    CyberInput(
                        value = span,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) span = it },
                        placeholder = "持续节数"
                    )
                    
                    // 颜色预览 - 点击方块打开方形颜色选择器
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(ColorUtils.parseColor(colorHex), RoundedCornerShape(4.dp))
                                .border(2.dp, CyberTheme.colors.primary, RoundedCornerShape(4.dp))
                                .clickable { showSquarePicker = true }
                        )
                        CyberInput(
                            value = colorHex,
                            onValueChange = { colorHex = it },
                            placeholder = "#RRGGBB",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            buttons = {
                CyberButton(
                    text = "取消",
                    onClick = onDismiss,
                    isPrimary = false
                )
                Spacer(modifier = Modifier.width(16.dp))
                CyberButton(
                    text = "添加",
                    onClick = {
                        if (courseName.isNotEmpty()) {
                            val newCourse = CourseEntity(
                                id = UUID.randomUUID().toString(),
                                name = courseName,
                                location = location,
                                teacher = teacher,
                                dayOfWeek = dayOfWeek,
                                startLesson = startLesson,
                                span = span.toIntOrNull()?.coerceIn(1, 12) ?: 1,
                                colorHex = colorHex
                            )
                            viewModel.addCourse(newCourse)
                            onDismiss()
                        }
                    },
                    isPrimary = true
                )
            }
        )
    }
}

@Composable
fun DeleteConfirmDialog(
    course: CourseEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    CyberTheme {
        CyberDialog(
            onDismissRequest = onDismiss,
            title = {
                CyberText(
                    text = "确认删除",
                    style = CyberTheme.typography.headline
                )
            },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CyberText(
                        text = "确定要删除课程 \"${course.name}\" 吗？",
                        style = CyberTheme.typography.body,
                        color = CyberTheme.colors.text
                    )
                    CyberText(
                        text = "此操作无法撤销",
                        style = CyberTheme.typography.body.copy(fontSize = 12.sp),
                        color = CyberTheme.colors.textSecondary
                    )
                }
            },
            buttons = {
                CyberButton(
                    text = "取消",
                    onClick = onDismiss,
                    isPrimary = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                CyberButton(
                    text = "删除",
                    onClick = onConfirm,
                    isPrimary = false
                )
            }
        )
    }
}

@Composable
fun EditTimeDialog(
    lessonIndex: Int,
    currentTime: String,
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit
) {
    // 解析当前时间 "HH:MM\nHH:MM"
    val times = currentTime.split("\n")
    var startTime by remember { mutableStateOf(if (times.isNotEmpty()) times[0] else "08:00") }
    var endTime by remember { mutableStateOf(if (times.size >= 2) times[1] else "08:45") }
    
    CyberTheme {
        CyberDialog(
            onDismissRequest = onDismiss,
            title = {
                CyberText(
                    text = "编辑第${lessonIndex + 1}节时间",
                    style = CyberTheme.typography.headline
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberText(
                        text = "开始时间",
                        style = CyberTheme.typography.body.copy(fontSize = 14.sp),
                        color = CyberTheme.colors.text
                    )
                    CyberInput(
                        value = startTime,
                        onValueChange = { startTime = it },
                        placeholder = "HH:MM"
                    )
                    
                    CyberText(
                        text = "结束时间",
                        style = CyberTheme.typography.body.copy(fontSize = 14.sp),
                        color = CyberTheme.colors.text,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    CyberInput(
                        value = endTime,
                        onValueChange = { endTime = it },
                        placeholder = "HH:MM"
                    )
                }
            },
            buttons = {
                CyberButton(
                    text = "取消",
                    onClick = onDismiss,
                    isPrimary = false
                )
                Spacer(modifier = Modifier.width(16.dp))
                CyberButton(
                    text = "保存",
                    onClick = {
                        viewModel.updateLessonTime(lessonIndex, startTime, endTime)
                        onDismiss()
                    },
                    isPrimary = true
                )
            }
        )
    }
}
