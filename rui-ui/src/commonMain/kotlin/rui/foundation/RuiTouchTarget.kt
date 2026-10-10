package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntSize
import rui.theme.RuiTheme

/**
 * Reserves at least [rui.theme.RuiDimens.touchTarget] in both dimensions for
 * the node and places the visual inside it per [alignment]. Visual sizes below
 * the token — a `Sm` button's 32dp chrome — keep their measured size, while
 * the layout slot grows to the touch target, leaving adjacent controls room
 * for the touch region the OS input layer expands past a small control's
 * visual bounds on its own.
 *
 * [alignment] anchors the visual inside the reserved slot: the default
 * [Alignment.Center] keeps chrome centered (buttons), while an edge anchor
 * keeps a glyph pinned to the container edge and lets the reservation extend
 * toward the content (text field action slots).
 *
 * Incoming constraints win over the token: inside a tighter parent (exact
 * sizing, `weight`) the node keeps the parent's size instead of overflowing
 * it. Place this modifier before the visual sizing — `.ruiMinimumTouchTarget()
 * .size(…)` — so the chrome is what gets placed.
 */
@Composable
public fun Modifier.ruiMinimumTouchTarget(
    alignment: Alignment = Alignment.Center,
): Modifier {
    val touchTarget = RuiTheme.dimens.touchTarget

    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val minimum = touchTarget.roundToPx()
        var width = maxOf(placeable.width, minimum)
        var height = maxOf(placeable.height, minimum)
        if (placeable.width < minimum) width = width.coerceIn(constraints.minWidth, constraints.maxWidth)
        if (placeable.height < minimum) height = height.coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            placeable.placeRelative(
                alignment.align(
                    size = IntSize(placeable.width, placeable.height),
                    space = IntSize(width, height),
                    layoutDirection = layoutDirection,
                ),
            )
        }
    }
}
