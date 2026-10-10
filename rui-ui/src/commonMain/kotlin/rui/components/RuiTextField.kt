package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import rui.foundation.ruiMinimumTouchTarget
import rui.theme.RuiTheme

/**
 * The Rui text field: Unstyled's state-driven input (hoisted [TextFieldState])
 * in a filled, borderless container — separation comes from the fill color,
 * a hairline appears only to mark the error state.
 *
 * Slot contract: [trailing] is the action slot — its content is wrapped in a
 * touch-target reservation anchored to the field's edge, so an icon-sized
 * clear or toggle keeps a full [rui.theme.RuiDimens.touchTarget] slot without
 * floating away from the edge. Keep [leading] decorative (a magnifier, a
 * domain icon); for a clickable leading action pass a [RuiIconButton], which
 * reserves its own touch target.
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

    val contentColor = if (enabled) colors.content else colors.contentDisabled

    Column(modifier = modifier) {
        if (label != null) {
            RuiText(
                text = label,
                modifier = Modifier.padding(bottom = spacing.xs),
                style = typography.labelMd,
                color = if (isError) colors.danger else colors.contentMuted,
            )
        }

        UnstyledTextField(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.touchTarget)
                .clip(shape)
                .background(colors.surfaceMuted, shape)
                .then(
                    if (isError) {
                        Modifier.border(dimens.borderWidth, colors.danger, shape)
                    } else {
                        Modifier
                    },
                )
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
                    Box(modifier = Modifier.ruiMinimumTouchTarget(Alignment.CenterEnd)) {
                        trailing()
                    }
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
