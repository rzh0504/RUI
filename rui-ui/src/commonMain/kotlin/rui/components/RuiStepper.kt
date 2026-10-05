package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/** Height of the stepper capsule; matches the small icon button tier. */
private val StepperHeight = 32.dp

/** Width of one stepper segment. */
private val StepperSegmentWidth = 40.dp

/** Width of the middle value readout; fits up to three digits. */
private val StepperValueWidth = 48.dp

/**
 * The compact counter: a muted stadium of `− | value | +` segments split by
 * hairlines, the current value readable in the middle. The − and + segments
 * disable themselves at the bounds of [range], so the value always stays
 * inside it — the caller never clamps.
 *
 * Tapping steps by one; there is no hold-to-repeat.
 *
 * @param value The hoisted count.
 * @param range The inclusive bounds the value is kept within.
 * @param showValue Renders the current value between the segments; without
 *   it the stepper is the bare − / + pair and the value lives elsewhere.
 */
@Composable
public fun RuiStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..Int.MAX_VALUE,
    enabled: Boolean = true,
    showValue: Boolean = true,
) {
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val typography = RuiTheme.typography

    Row(
        modifier = modifier
            .height(StepperHeight)
            .clip(RuiTheme.shapes.full)
            .background(colors.surfaceMuted),
    ) {
        StepperSegment(
            icon = RuiIcons.Minus,
            enabled = enabled && value > range.first,
            onClickLabel = "Decrease",
            onClick = { onValueChange((value - 1).coerceIn(range)) },
            modifier = Modifier
                .width(StepperSegmentWidth)
                .fillMaxHeight(),
        )
        if (showValue) {
            Box(
                modifier = Modifier
                    .width(StepperValueWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                RuiText(
                    text = value.toString(),
                    style = typography.bodyLg,
                    color = if (enabled) colors.content else colors.contentDisabled,
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .width(dimens.borderWidth)
                    .fillMaxHeight()
                    .background(colors.outlineSubtle),
            )
        }
        StepperSegment(
            icon = RuiIcons.Plus,
            enabled = enabled && value < range.last,
            onClickLabel = "Increase",
            onClick = { onValueChange((value + 1).coerceIn(range)) },
            modifier = Modifier
                .width(StepperSegmentWidth)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun StepperSegment(
    icon: ImageVector,
    enabled: Boolean,
    onClickLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RuiTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRuiIndication(),
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        RuiIcon(
            imageVector = icon,
            contentDescription = null,
            size = 16.dp,
            tint = if (enabled) colors.content else colors.contentDisabled,
        )
    }
}
