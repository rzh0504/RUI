package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
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
    minimumWidthPx: Int,
    minimumHeightPx: Int,
    alignment: Alignment,
    layoutDirection: LayoutDirection,
    minWidth: Int = 0,
    maxWidth: Int = Constraints.Infinity,
    minHeight: Int = 0,
    maxHeight: Int = Constraints.Infinity,
): TouchTargetPlacement {
    val width = maxOf(contentWidth, minimumWidthPx).coerceIn(minWidth, maxWidth)
    val height = maxOf(contentHeight, minimumHeightPx).coerceIn(minHeight, maxHeight)
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
 * the layout slot grows to the touch target. Place this modifier outside the
 * clickable/selectable modifier so semantics and pointer input are attached
 * to the expanded layout node.
 *
 * [alignment] anchors the visual inside the reserved slot: the default
 * [Alignment.Center] keeps chrome centered (buttons), while an edge anchor
 * keeps a glyph pinned to the container edge and lets the reservation extend
 * toward the content (text field action slots).
 *
 * Incoming constraints win over the requested minimums: inside a tighter parent (exact
 * sizing, `weight`) the node keeps the parent's size instead of overflowing
 * it. Place this modifier before the visual sizing — `.ruiMinimumTouchTarget()
 * .size(…)` — so the chrome is what gets placed.
 */
@Composable
public fun Modifier.ruiMinimumTouchTarget(
    alignment: Alignment = Alignment.Center,
    minWidth: Dp = RuiTheme.dimens.touchTarget,
    minHeight: Dp = RuiTheme.dimens.touchTarget,
): Modifier {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val placement = touchTargetPlacement(
            contentWidth = placeable.width,
            contentHeight = placeable.height,
            minimumWidthPx = minWidth.roundToPx(),
            minimumHeightPx = minHeight.roundToPx(),
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
