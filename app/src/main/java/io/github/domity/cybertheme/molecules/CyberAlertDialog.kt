package io.github.domity.cybertheme.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.domity.cybertheme.atoms.CyberSurface
import io.github.domity.cybertheme.atoms.CyberText
import io.github.domity.cybertheme.foundation.CyberTheme

@Composable
fun CyberAlertDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest) {
        CyberSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = CutCornerShape(12.dp),
            color = CyberTheme.colors.surface,
            borderWidth = 1.dp,
            borderColor = CyberTheme.colors.border
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Title
                if (title != null) {
                    CyberText(
                        text = title,
                        style = CyberTheme.typography.headline.copy(
                            fontSize = CyberTheme.typography.headline.fontSize * 0.7f
                        ),
                        color = CyberTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Content
                if (content != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = content
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Buttons
                if (confirmButton != null || dismissButton != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissButton != null) {
                            dismissButton()
                        }
                        if (confirmButton != null && dismissButton != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        if (confirmButton != null) {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = CyberTheme.colors.primary
) {
    Box(
        modifier = modifier
            .clip(CutCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CyberText(
            text = text.uppercase(),
            style = CyberTheme.typography.button.copy(
                fontSize = CyberTheme.typography.button.fontSize * 0.85f
            ),
            color = if (enabled) color else color.copy(alpha = 0.4f)
        )
    }
}
