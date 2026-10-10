package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import rui.theme.RuiTheme

/**
 * Reserves at least [rui.theme.RuiDimens.touchTarget] in both dimensions for
 * the node and centers the visual inside it. Visual sizes below the token — a
 * `Sm` button's 32dp chrome — keep their measured size, while the layout slot
 * grows to the touch target, leaving adjacent controls room for the touch
 * region the OS input layer expands past a small control's visual bounds on
 * its own.
 *
 * Incoming constraints win over the token: inside a tighter parent (exact
 * sizing, `weight`) the node keeps the parent's size instead of overflowing
 * it. Place this modifier before the visual sizing — `.ruiMinimumTouchTarget()
 * .size(…)` — so the chrome is what gets centered.
 */
@Composable
public fun Modifier.ruiMinimumTouchTarget(): Modifier {
    val touchTarget = RuiTheme.dimens.touchTarget

    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val minimum = touchTarget.roundToPx()
        var width = maxOf(placeable.width, minimum)
        var height = maxOf(placeable.height, minimum)
        if (placeable.width < minimum) {
            width = width.coerceIn(constraints.minWidth, constraints.maxWidth)
        }
        if (placeable.height < minimum) {
            height = height.coerceIn(constraints.minHeight, constraints.maxHeight)
        }
        layout(width, height) {
            placeable.placeRelative(
                (width - placeable.width) / 2,
                (height - placeable.height) / 2,
            )
        }
    }
}
