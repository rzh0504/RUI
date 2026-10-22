package rui.foundation

import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.composeunstyled.focusRing
import com.composeunstyled.theme.ColoredIndication
import rui.theme.RuiTheme
import rui.theme.StateLayerAlphas

/**
 * The Rui state layer: a content-colored alpha overlay for pressed / hovered /
 * focused / dragged interactions. Material's feedback discipline without its
 * tonal system — the same recipe on every platform.
 */
@Composable
public fun rememberRuiIndication(): Indication {
    val contentColor = RuiTheme.colors.content
    val motion = RuiTheme.motion

    return remember(contentColor, motion) {
        ColoredIndication(
            hoveredColor = contentColor.copy(alpha = StateLayerAlphas.hovered),
            pressedColor = contentColor.copy(alpha = StateLayerAlphas.pressed),
            focusedColor = contentColor.copy(alpha = StateLayerAlphas.focused),
            draggedColor = contentColor.copy(alpha = StateLayerAlphas.dragged),
            animationSpecEnter = motion.stateLayerIn,
            animationSpecExit = motion.stateLayerOut,
        )
    }
}

/**
 * Draws the keyboard focus ring around the component using the theme's focus
 * color and ring width. Visibility follows keyboard-only focus by default, so
 * pointer clicks never show a ring.
 */
@Composable
public fun Modifier.ruiFocusRing(
    interactionSource: MutableInteractionSource,
    shape: Shape = RuiTheme.shapes.md,
): Modifier {
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens

    return focusRing(
        interactionSource = interactionSource,
        width = dimens.focusRingWidth,
        color = colors.primary,
        shape = shape,
    )
}
