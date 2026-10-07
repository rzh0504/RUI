package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.ColoredIndication
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.rememberRuiHaptics
import rui.foundation.ruiFocusRing
import rui.theme.RuiTheme
import rui.theme.StateLayerAlphas

/** Height of the chip capsule; matches the stepper capsule. */
private val ChipHeight = 32.dp

/** Side of the dismiss affordance's square hot zone, centered on the icon. */
private val DismissHotSpot = 32.dp

/**
 * Colors of [RuiChip] across the selection and enabled states.
 */
@Immutable
public data class RuiChipColors(
    public val container: Color,
    public val content: Color,
    public val selectedContainer: Color,
    public val selectedContent: Color,
    public val disabledContainer: Color,
    public val disabledContent: Color,
)

public object RuiChipDefaults {

    /**
     * The default chip colors. With [selected], the [RuiChipColors.container] /
     * [RuiChipColors.content] pair mirrors the selected state, so callers
     * reading the pair outside the chip see what a selected chip renders.
     */
    @Composable
    public fun colors(selected: Boolean = false): RuiChipColors {
        val colors = RuiTheme.colors
        return RuiChipColors(
            container = if (selected) colors.primary else colors.surfaceMuted,
            content = if (selected) colors.onPrimary else colors.content,
            selectedContainer = colors.primary,
            selectedContent = colors.onPrimary,
            disabledContainer = colors.surfaceMuted,
            disabledContent = colors.contentDisabled,
        )
    }
}

/**
 * The compact capsule label: a filter chip when [onClick] is set, a plain tag
 * otherwise. The capsule is 32dp tall (the stepper tier); in flows, leave
 * `spacing.md` between rows so adjacent chips keep a 44dp touch pitch.
 *
 * - [onClick] set — the whole capsule toggles [selected], visually and in
 *   selection semantics, and ticks the selection haptic.
 * - [onClick] null — static display: no press state, no selection semantics;
 *   [onDismiss] still works, so a removable informational tag stays possible.
 * - [onDismiss] set — a trailing close affordance with its own hot zone,
 *   labeled by [RuiTheme.strings]' `remove` string.
 *
 * @param text The capsule's label.
 * @param selected Whether the chip renders (and announces) as selected.
 * @param enabled Whether the chip and its dismiss affordance respond.
 * @param leading Slot before the text, sized like a small icon by the caller.
 * @param colors Visual colors, see [RuiChipDefaults.colors].
 */
@Composable
public fun RuiChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    colors: RuiChipColors = RuiChipDefaults.colors(selected),
) {
    val haptics = rememberRuiHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val container = when {
        !enabled -> colors.disabledContainer
        selected -> colors.selectedContainer
        else -> colors.container
    }
    val contentColor = when {
        !enabled -> colors.disabledContent
        selected -> colors.selectedContent
        else -> colors.content
    }
    val motion = RuiTheme.motion
    val indication = remember(contentColor, motion) {
        ColoredIndication(
            hoveredColor = contentColor.copy(alpha = StateLayerAlphas.hovered),
            pressedColor = contentColor.copy(alpha = StateLayerAlphas.pressed),
            focusedColor = contentColor.copy(alpha = StateLayerAlphas.focused),
            draggedColor = contentColor.copy(alpha = StateLayerAlphas.dragged),
            animationSpecEnter = motion.stateLayerIn,
            animationSpecExit = motion.stateLayerOut,
        )
    }

    val interactive = onClick != null && enabled
    Row(
        modifier = modifier
            .then(
                if (interactive) {
                    Modifier
                        .selectable(
                            selected = selected,
                            interactionSource = interactionSource,
                            indication = indication,
                            enabled = enabled,
                            role = Role.Button,
                            onClick = {
                                haptics.selection()
                                onClick()
                            },
                        )
                        .ruiFocusRing(interactionSource, RuiTheme.shapes.full)
                } else {
                    Modifier
                },
            )
            .height(ChipHeight)
            .clip(RuiTheme.shapes.full)
            .background(container, RuiTheme.shapes.full),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(modifier = Modifier.padding(start = RuiTheme.spacing.md)) {
                leading()
            }
        }
        RuiText(
            text = text,
            style = RuiTheme.typography.labelLg,
            color = contentColor,
            maxLines = 1,
            modifier = Modifier.padding(
                start = if (leading != null) RuiTheme.spacing.sm else RuiTheme.spacing.md,
                end = if (onDismiss != null) RuiTheme.spacing.sm else RuiTheme.spacing.md,
            ),
        )
        if (onDismiss != null) {
            val dismissInteractionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(DismissHotSpot)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = dismissInteractionSource,
                        indication = indication,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onDismiss,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                RuiIcon(
                    imageVector = RuiIcons.Close,
                    contentDescription = RuiTheme.strings.remove,
                    size = 16.dp,
                    tint = contentColor,
                )
            }
        }
    }
}
