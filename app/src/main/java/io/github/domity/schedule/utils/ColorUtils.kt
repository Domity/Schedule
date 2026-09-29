package io.github.domity.schedule.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

object ColorUtils {
    /**
     * hex color to compose color
     */
    fun parseColor(colorHex: String): Color {
        return try {
            Color(colorHex.toColorInt())
        } catch (_: Exception) {
            Color(0xFF107C41) // 万一呢
        }
    }
//  创建砖块中间的透明颜色
    fun getTransparentColor(color: Color, alpha: Float = 0.20f): Color {
        return color.copy(alpha = alpha)
    }

}
