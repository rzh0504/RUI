package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import rui.theme.RuiTheme

/**
 * The measured result of a touch-target-reserved node: the node's size and
 * where the visual content lands inside it.
 */
internal data class TouchTargetPlacement(
    val width: Int,
    val height: Int,
    val contentOffsetX: Int,
    val contentOffsetY: Int,
)

/**
 * The reserve-and-anchor math behind [ruiMinimumTouchTarget], pure so the
 * geometry rules are testable: the node grows to [minimumPx] in each
 * dimension, a parent constraining tighter than the token wins (the node
 * keeps the parent's size instead of overflowing it), and the content lands
 * per [alignment] inside the reserved node.
 */
internal fun touchTargetPlacement(
    contentWidth: Int,
    contentHeight: Int,
    minimumPx: Int,
    alignment: Alignment,
    layoutDirection: LayoutDirection,
    minWidth: Int = 0,
    maxWidth: Int = Constraints.Infinity,
    minHeight: Int = 0,
    maxHeight: Int = Constraints.Infinity,
): TouchTargetPlacement {
    var width = maxOf(contentWidth, minimumPx)
    var height = maxOf(contentHeight, minimumPx)
    if (contentWidth < minimumPx) width = width.coerceIn(minWidth, maxWidth)
    if (contentHeight < minimumPx) height = height.coerceIn(minHeight, maxHeight)
    val offset = alignment.align(
        size = IntSize(contentWidth, contentHeight),
        space = IntSize(width, height),
        layoutDirection = layoutDirection,
    )
    return TouchTargetPlacement(width, height, offset.x, offset.y)
}

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
        val placement = touchTargetPlacement(
            contentWidth = placeable.width,
            contentHeight = placeable.height,
            minimumPx = touchTarget.roundToPx(),
            alignment = alignment,
            layoutDirection = layoutDirection,
            minWidth = constraints.minWidth,
            maxWidth = constraints.maxWidth,
            minHeight = constraints.minHeight,
            maxHeight = constraints.maxHeight,
        )
        layout(placement.width, placement.height) {
            placeable.placeRelative(placement.contentOffsetX, placement.contentOffsetY)
        }
    }
}
