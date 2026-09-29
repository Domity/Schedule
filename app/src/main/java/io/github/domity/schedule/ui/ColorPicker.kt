package io.github.domity.schedule.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.domity.cybertheme.atoms.CyberText
import io.github.domity.cybertheme.foundation.CyberTheme
import io.github.domity.cybertheme.molecules.CyberButton
import io.github.domity.cybertheme.molecules.CyberDialog
import io.github.domity.cybertheme.molecules.CyberInput
import io.github.domity.schedule.utils.ColorUtils
import java.util.Locale

private val HEX_PATTERN = Regex("^#[0-9A-Fa-f]{6}$")

fun isValidHex(hex: String): Boolean = HEX_PATTERN.matches(hex)

fun formatHexFromHsv(hue: Float, sat: Float, value: Float): String {
    val argb = AndroidColor.HSVToColor(floatArrayOf(hue, sat, value))
    return String.format(Locale.US, "#%06X", 0xFFFFFF and argb)
}

/**
 * 方形 HSV 颜色选择器（饱和度/明度方块 + 色相条）
 * 通过 [colorHex] 与外部双向实时同步。
 */
@Composable
fun SquareColorPicker(
    colorHex: String,
    onColorChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var sat by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }

    // 当外部颜色变化（例如手动输入 hex）时，反向同步 HSV。
    // 仅当外部颜色合法且与当前渲染不一致时才更新，避免抖动与死循环。
    LaunchedEffect(colorHex) {
        if (!isValidHex(colorHex)) return@LaunchedEffect
        val target = ColorUtils.parseColor(colorHex).toArgb()
        val current = AndroidColor.HSVToColor(floatArrayOf(hue, sat, value))
        if (current != target) {
            val hsv = FloatArray(3)
            AndroidColor.colorToHSV(target, hsv)
            hue = hsv[0]
            sat = hsv[1]
            value = hsv[2]
        }
    }

    fun emit() {
        onColorChange(formatHexFromHsv(hue, sat, value))
    }

    val hueColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 饱和度 / 明度方块
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, CyberTheme.colors.border, RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        sat = (offset.x / size.width).coerceIn(0f, 1f)
                        value = (1f - offset.y / size.height).coerceIn(0f, 1f)
                        emit()
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            sat = (offset.x / size.width).coerceIn(0f, 1f)
                            value = (1f - offset.y / size.height).coerceIn(0f, 1f)
                            emit()
                        },
                        onDrag = { change, _ ->
                            sat = (change.position.x / size.width).coerceIn(0f, 1f)
                            value = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                            emit()
                            change.consume()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(hueColor)
                drawRect(Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))

                val cx = sat * size.width
                val cy = (1f - value) * size.height
                val radius = 9.dp.toPx()
                drawCircle(Color.Black, radius = radius, center = Offset(cx, cy), style = Stroke(4.dp.toPx()))
                drawCircle(Color.White, radius = radius, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
            }
        }

        // 色相滑条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, CyberTheme.colors.border, RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        hue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                        emit()
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            hue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                            emit()
                        },
                        onDrag = { change, _ ->
                            hue = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                            emit()
                            change.consume()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rainbow = (0..360 step 30).map {
                    Color(AndroidColor.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f)))
                }
                drawRect(Brush.horizontalGradient(rainbow))

                val x = (hue / 360f).coerceIn(0f, 1f) * size.width
                val centerY = size.height / 2f
                drawCircle(Color.Black, radius = 11.dp.toPx(), center = Offset(x, centerY), style = Stroke(4.dp.toPx()))
                drawCircle(Color.White, radius = 11.dp.toPx(), center = Offset(x, centerY), style = Stroke(2.dp.toPx()))
            }
        }
    }
}

/**
 * 方形颜色选择器弹窗
 * 颜色变化通过 [onColorChange] 实时回传；取消时回滚到打开时的颜色。
 */
@Composable
fun SquareColorPickerDialog(
    colorHex: String,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "颜色选择器"
) {
    val predefinedColors = listOf(
        "#FF5733", "#33FF57", "#3357FF", "#A5A6FD",
        "#FFFF00", "#33FFF5", "#FF8C33", "#FF00FF",
        "#33FF8C", "#FF0000", "#00FF00", "#0000FF"
    )

    // 打开时的快照，用于取消时回滚
    val snapshot = remember { colorHex }

    CyberTheme {
        CyberDialog(
            onDismissRequest = {
                onColorChange(snapshot)
                onDismiss()
            },
            title = {
                CyberText(
                    text = title,
                    style = CyberTheme.typography.headline
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 预览 + Hex 输入
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(ColorUtils.parseColor(colorHex), RoundedCornerShape(6.dp))
                                .border(1.dp, CyberTheme.colors.border, RoundedCornerShape(6.dp))
                        )
                        CyberInput(
                            value = colorHex,
                            onValueChange = onColorChange,
                            placeholder = "#RRGGBB",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 预设色板
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        predefinedColors.chunked(6).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowColors.forEach { hex ->
                                    val color = ColorUtils.parseColor(hex)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(color, RoundedCornerShape(4.dp))
                                            .border(
                                                width = if (colorHex.equals(hex, ignoreCase = true)) 3.dp else 1.dp,
                                                color = if (colorHex.equals(hex, ignoreCase = true)) CyberTheme.colors.primary else CyberTheme.colors.border,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable { onColorChange(hex) }
                                    )
                                }
                            }
                        }
                    }

                    // 方形取色器
                    SquareColorPicker(
                        colorHex = colorHex,
                        onColorChange = onColorChange
                    )
                }
            },
            buttons = {
                CyberButton(
                    text = "取消",
                    onClick = {
                        onColorChange(snapshot)
                        onDismiss()
                    },
                    isPrimary = false
                )
                Spacer(modifier = Modifier.width(16.dp))
                CyberButton(
                    text = "确定",
                    onClick = onDismiss,
                    isPrimary = true
                )
            }
        )
    }
}
