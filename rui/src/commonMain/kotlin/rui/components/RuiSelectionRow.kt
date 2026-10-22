package rui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import rui.foundation.BlurBackdrop
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.theme.RuiTheme

/** One option of a [RuiSelectionRow]. */
@Immutable
public data class RuiSelectionOption<T>(
    public val value: T,
    public val label: String,
)

/**
 * A settings value row that picks from a dropdown: the row reads as a value
 * row — title, the current option's label in the value slot, chevron — and
 * tapping it drops down the option menu anchored to the row, checkmark on
 * the current value, iOS's in-menu picker for settings pages. The menu
 * spans the row's width, so it reads as part of the settings card.
 * Selecting an option closes the menu and reports through [onSelect].
 *
 * The menu is the standard [RuiDropdownMenu]: it portals above everything
 * through the overlay host and dismisses on outside tap, back and Esc.
 * Give it the screen's [backdrop] and the active [material] to render in
 * the app's glass material.
 *
 * @param selected The currently selected value; the row's value slot shows
 *   the first option matching it. A value with no matching option shows an
 *   empty value slot until a selection is made.
 */
@Composable
public fun <T> RuiSelectionRow(
    title: String,
    options: List<RuiSelectionOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.value == selected }?.label

    RuiDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = modifier,
        // The menu hugs the value side: right-aligned under the current
        // option's label and chevron, growing leftward with its content —
        // iOS's in-row menu placement.
        alignment = AnchorAlignment.End,
        backdrop = backdrop,
        material = material,
        anchor = {
            // The popup right-aligns to the measured anchor, so measure the
            // row a hair narrower than it draws — the menu clears the card's
            // edge instead of touching it.
            Box(
                Modifier.layout { measurable, constraints ->
                    val inset = 8.dp.roundToPx()
                    val placeable = measurable.measure(
                        constraints.copy(maxWidth = (constraints.maxWidth - inset).coerceAtLeast(0)),
                    )
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                },
            ) {
                RuiListItem(
                    title = title,
                    value = currentLabel,
                    showChevron = true,
                    onClick = { expanded = true },
                )
            }
        },
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            RuiDropdownMenuItem(
                text = option.label,
                onClick = {
                    onSelect(option.value)
                    expanded = false
                },
                trailing = {
                    // Every row reserves the checkmark's slot — unselected
                    // rows draw it transparent — so selecting an option
                    // never re-measures the panel, whose width would
                    // otherwise jump as the checkmark hops rows on the way
                    // out.
                    RuiIcon(
                        imageVector = RuiIcons.Check,
                        contentDescription = null,
                        tint = if (isSelected) RuiTheme.colors.primary else Color.Transparent,
                    )
                },
            )
        }
    }
}
