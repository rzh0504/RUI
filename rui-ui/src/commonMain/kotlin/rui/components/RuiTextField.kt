package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import com.composeunstyled.TextInput
import com.composeunstyled.UnstyledTextField
import rui.foundation.RuiText
import rui.foundation.ruiFocusRing
import rui.theme.RuiTheme

/**
 * The Rui text field: Unstyled's state-driven input (hoisted [TextFieldState])
 * wrapped in a labeled, bordered surface with helper/error text support.
 */
@Composable
public fun RuiTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = RuiTheme.shapes.md,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing
    val dimens = RuiTheme.dimens

    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused by resolvedInteractionSource.collectIsFocusedAsState()

    val borderColor = when {
        isError -> colors.danger
        focused -> colors.primary
        else -> colors.outline
    }
    val containerColor = if (enabled) colors.surface else colors.surfaceMuted
    val contentColor = when {
        !enabled -> colors.contentDisabled
        isError -> colors.danger
        else -> colors.content
    }

    Column(modifier = modifier) {
        if (label != null) {
            RuiText(
                text = label,
                modifier = Modifier.padding(bottom = spacing.xs),
                style = typography.labelMd,
                color = colors.contentMuted,
            )
        }

        UnstyledTextField(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.touchTarget)
                .clip(shape)
                .background(containerColor, shape)
                .border(dimens.borderWidth, borderColor, shape)
                .ruiFocusRing(resolvedInteractionSource, shape),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = typography.bodyMd,
            textColor = contentColor,
            cursorBrush = SolidColor(colors.primary),
            selectionColors = TextSelectionColors(
                handleColor = colors.primary,
                backgroundColor = colors.primary.copy(alpha = 0.2f),
            ),
            interactionSource = resolvedInteractionSource,
            keyboardOptions = keyboardOptions,
            lineLimits = lineLimits,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leading != null) {
                    leading()
                }
                TextInput(modifier = Modifier.weight(1f)) {
                    if (placeholder != null) {
                        RuiText(text = placeholder, color = colors.contentMuted)
                    }
                }
                if (trailing != null) {
                    trailing()
                }
            }
        }

        if (supportingText != null) {
            RuiText(
                text = supportingText,
                modifier = Modifier.padding(top = spacing.xs),
                style = typography.labelSm,
                color = if (isError) colors.danger else colors.contentMuted,
            )
        }
    }
}
