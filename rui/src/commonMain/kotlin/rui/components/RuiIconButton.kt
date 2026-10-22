package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.ColoredIndication
import rui.foundation.ruiFocusRing
import rui.foundation.ruiMinimumTouchTarget
import rui.theme.RuiTheme
import rui.theme.StateLayerAlphas

/**
 * A square [RuiButton] for a single icon. The icon slot is rendered with the
 * resolved content color, so a raw [rui.foundation.RuiIcon] without a tint
 * already looks right.
 */
@Composable
public fun RuiIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RuiButtonVariant = RuiButtonVariant.Tonal,
    size: RuiButtonSize = RuiButtonSize.Md,
    enabled: Boolean = true,
    shape: Shape = RuiTheme.shapes.md,
    colors: RuiButtonColors = RuiButtonDefaults.colors(variant, enabled),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val motion = RuiTheme.motion

    val stateLayer = remember(colors.content, motion) {
        ColoredIndication(
            hoveredColor = colors.content.copy(alpha = StateLayerAlphas.hovered),
            pressedColor = colors.content.copy(alpha = StateLayerAlphas.pressed),
            focusedColor = colors.content.copy(alpha = StateLayerAlphas.focused),
            draggedColor = colors.content.copy(alpha = StateLayerAlphas.dragged),
            animationSpecEnter = motion.stateLayerIn,
            animationSpecExit = motion.stateLayerOut,
        )
    }

    UnstyledButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .ruiMinimumTouchTarget()
            .size(RuiButtonDefaults.iconButtonSize(size))
            .clip(shape)
            .background(colors.container, shape)
            .ruiFocusRing(resolvedInteractionSource, shape),
        role = Role.Button,
        indication = stateLayer,
        interactionSource = resolvedInteractionSource,
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.content) {
            content()
        }
    }
}

/**
 * Square visual side length matching [RuiButtonDefaults.minHeight] for the
 * same tier. This is the chrome, not the touch slot: the button reserves
 * [rui.theme.RuiDimens.touchTarget] around it regardless of the tier.
 */
public fun RuiButtonDefaults.iconButtonSize(size: RuiButtonSize): Dp = when (size) {
    RuiButtonSize.Sm -> 32.dp
    RuiButtonSize.Md -> 40.dp
    RuiButtonSize.Lg -> 48.dp
}
