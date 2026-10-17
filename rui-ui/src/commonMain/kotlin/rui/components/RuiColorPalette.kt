package rui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.foundation.ruiMinimumTouchTarget
import rui.theme.RuiTheme

/**
 * An iOS-style color palette: a grid of circular swatches drawn from a
 * caller-provided list — tag colors, category accents, theme choices — with
 * the selected one wrapped in an accent ring, the way UIKit marks a picked
 * color. The caller owns the palette (Rui renders what it is given; there is
 * no generated HSV grid here) and the selected color; swatches compare by
 * [Color] equality, so duplicate swatches light up together.
 *
 * Light swatches get a hairline outline so they stay visible on the page,
 * and every swatch reserves the minimum touch target regardless of
 * [swatchSize].
 *
 * @param colors The palette, laid out left-to-right then down, [columns] per
 *   row; an empty list renders nothing.
 * @param selected The currently selected color, or null for no selection.
 * @param onSelect Called with the tapped swatch's color.
 * @param columns Swatches per row; the last row leaves its tail empty at the
 *   same cell size as every other row.
 * @param swatchSize The visible diameter of each swatch.
 * @param enabled Whether swatches respond to taps; disabled swatches render
 *   at reduced opacity and ignore taps.
 */
@Composable
public fun RuiColorPalette(
    colors: List<Color>,
    selected: Color?,
    onSelect: (Color) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5,
    swatchSize: Dp = 34.dp,
    enabled: Boolean = true,
) {
    require(columns >= 1) { "columns should be >= 1" }
    val themeColors = RuiTheme.colors
    val spacing = RuiTheme.spacing.sm
    val borderWidth = RuiTheme.dimens.borderWidth
    val selectionGap = 3.dp
    val selectionStroke = 2.dp
    val onSelectState = rememberUpdatedState(onSelect)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        paletteRows(colors, columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                row.forEach { color ->
                    if (color == null) {
                        // Empty tail slot keeps the weighted cells — and the
                        // swatch sizes — equal across rows.
                        Box(Modifier.weight(1f))
                    } else {
                        val isSelected = color == selected
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .ruiMinimumTouchTarget()
                                    .size(swatchSize + selectionGap * 2 + selectionStroke)
                                    .then(
                                        if (enabled) {
                                            Modifier.selectable(
                                                selected = isSelected,
                                                role = Role.RadioButton,
                                                onClick = { onSelectState.value(color) },
                                            )
                                        } else {
                                            Modifier
                                        },
                                    ),
                            ) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val swatchRadius = swatchSize.toPx() / 2f
                                drawCircle(
                                    color = color.copy(alpha = if (enabled) 1f else 0.38f),
                                    radius = swatchRadius,
                                    center = center,
                                )
                                drawCircle(
                                    color = themeColors.outlineSubtle,
                                    radius = swatchRadius,
                                    center = center,
                                    style = Stroke(width = borderWidth.toPx()),
                                )
                                if (isSelected) {
                                    drawCircle(
                                        color = themeColors.primary,
                                        radius = swatchRadius + selectionGap.toPx(),
                                        center = center,
                                        style = Stroke(width = selectionStroke.toPx()),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Splits [colors] into rows of at most [columns] (clamped to at least 1),
 * padding the last row with nulls so every row carries [columns] cells.
 */
internal fun paletteRows(colors: List<Color>, columns: Int): List<List<Color?>> {
    val effectiveColumns = columns.coerceAtLeast(1)
    return colors.chunked(effectiveColumns).map { line ->
        if (line.size < effectiveColumns) {
            line + List(effectiveColumns - line.size) { null }
        } else {
            line
        }
    }
}
