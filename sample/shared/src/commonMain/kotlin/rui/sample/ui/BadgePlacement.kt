package rui.sample.ui

import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlin.math.sqrt

/**
 * Hangs a status dot on the rim of a circular anchor, along its end-corner
 * diagonal: a circle never reaches its bounding-box corner — along the
 * diagonal the corner sits r·(1 − 1/√2) beyond the rim — so the dot pulls
 * in by that sagitta minus half the dot, landing its center exactly on the
 * rim. Pair with `Modifier.align(Alignment.TopEnd / BottomEnd)`.
 */
internal fun Modifier.circleRimDotOffset(
    circleSize: Dp,
    dotSize: Dp,
    corner: Alignment,
): Modifier {
    val pull = circleSize / 2 * (1f - 1f / sqrt(2f)) - dotSize / 2
    val x = -pull
    val y = if (corner == Alignment.TopEnd) pull else -pull
    return offset(x = x, y = y)
}
