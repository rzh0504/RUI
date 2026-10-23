package rui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import rui.foundation.BlurBackdrop
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.theme.RuiTheme

/** How far inside the card the row's menu panel sits. */
private val MenuEdgeInset = 8.dp

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
 * aligns to the row's value side and is kept a hair inside the card's
 * trailing edge; it sizes to its options instead of stretching the card.
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
        // The menu hugs the value side: aligned under the current option's
        // label and chevron, inset from the card's edge so the panel reads
        // as floating instead of touching (or bleeding off) the card.
        alignment = AnchorAlignment.End,
        alignmentOffset = -MenuEdgeInset,
        backdrop = backdrop,
        material = material,
        anchor = {
            RuiListItem(
                title = title,
                value = currentLabel,
                showChevron = true,
                onClick = { expanded = true },
            )
        },
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            RuiDropdownMenuItem(
                text = option.label,
                modifier = Modifier.semantics { this.selected = isSelected },
                onClick = {
                    expanded = false
                    onSelect(option.value)
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
