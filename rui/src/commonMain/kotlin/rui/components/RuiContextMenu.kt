package rui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import rui.foundation.BlurBackdrop
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiGlassMaterial
import kotlin.math.roundToInt

/**
 * An iOS-style context menu: long-pressing (touch or mouse) or right-clicking
 * the [content] opens the [menu] at the press position — the same 250dp glass
 * panel (frosted, or liquid with `material = RuiGlassMaterial.Liquid`),
 * cascading [RuiDropdownSubmenu] levels and dismissal as [RuiDropdownMenu],
 * anchored where the finger landed and opening upward, so the panel never
 * sits under the finger. The content itself does not lift; outside tap, back
 * and Esc dismiss from any submenu depth.
 *
 * The gesture modifiers ride a wrapper around [content], so press handlers
 * inside it (clickable rows, toggles) keep working — only the long press and
 * the secondary click open the menu.
 *
 * @param menu The menu rows ([RuiDropdownMenuItem], [RuiDropdownSubmenu],
 *   plain [RuiDivider]).
 */
@ExperimentalRuiApi
@Composable
public fun RuiContextMenu(
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
    modifier: Modifier = Modifier,
    menu: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    var pressPoint by remember { mutableStateOf<Offset?>(null) }
    val open: (Offset) -> Unit = { pressPoint = it }

    Box(modifier) {
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset -> open(offset) },
                    )
                }
                .pointerInput(Unit) {
                    // Secondary click (right-click) opens where a long press
                    // would — the desktop form of the same interaction.
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                open(event.changes.first().position)
                                event.changes.forEach { it.consume() }
                                break
                            }
                            if (event.type == PointerEventType.Release) {
                                break
                            }
                        }
                    }
                },
        ) {
            content()
        }
        pressPoint?.let { point ->
            Box(
                modifier = Modifier.absoluteOffset {
                    IntOffset(point.x.roundToInt(), point.y.roundToInt())
                },
            ) {
                RuiDropdownMenu(
                    expanded = true,
                    onDismissRequest = { pressPoint = null },
                    backdrop = backdrop,
                    material = material,
                    side = AnchorSide.Top,
                    alignment = AnchorAlignment.Start,
                    anchor = { Box(Modifier.size(1.dp)) },
                    content = menu,
                )
            }
        }
    }
}
