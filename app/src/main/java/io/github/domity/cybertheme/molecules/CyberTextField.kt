package io.github.domity.cybertheme.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.domity.cybertheme.atoms.CyberText
import io.github.domity.cybertheme.foundation.CyberTheme

@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Label
        if (label != null) {
            CyberText(
                text = label,
                style = CyberTheme.typography.label.copy(fontSize = 12.sp),
                color = if (isFocused) CyberTheme.colors.primary else CyberTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // TextField
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = TextStyle(
                color = CyberTheme.colors.text,
                fontSize = 14.sp,
                fontFamily = CyberTheme.typography.body.fontFamily
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(CyberTheme.colors.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = CyberTheme.colors.background.copy(alpha = 0.3f),
                            shape = CutCornerShape(4.dp)
                        )
                        .border(
                            width = if (isFocused) 1.5.dp else 1.dp,
                            color = if (isFocused) CyberTheme.colors.primary else CyberTheme.colors.border,
                            shape = CutCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        CyberText(
                            text = placeholder,
                            style = CyberTheme.typography.body.copy(fontSize = 14.sp),
                            color = CyberTheme.colors.textSecondary.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
