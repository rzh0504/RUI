package rui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.theme.RuiTheme

/** The three states of a [RuiTriStateCheckbox]. */
public enum class RuiCheckboxState {
    /** Every affected item is selected. */
    On,

    /** No affected item is selected. */
    Off,

    /** Some affected items are selected — the parent's dash state. */
    Indeterminate,
}

/**
 * A checkbox with a partial-selection state for governing a group — the
 * "select all" parent. `On` and `Off` look exactly like [RuiCheckbox];
 * `Indeterminate` fills with the primary color and shows a dash. Tapping
 * cycles indeterminate → on → off, the standard parent behavior; the caller
 * applies the new state to the group it owns.
 *
 * The value is hoisted and the semantics carry the exact toggle state, so
 * screen readers announce "partially checked" where sighted users see the dash.
 */
@Composable
public fun RuiTriStateCheckbox(
    value: RuiCheckboxState,
    onValueChange: (RuiCheckboxState) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val motion = RuiTheme.motion
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val boxShape = RuiTheme.shapes.sm
    val filled = value != RuiCheckboxState.Off

    Box(
        modifier = modifier
            .size(dimens.touchTarget)
            .clickable(
                interactionSource = resolvedInteractionSource,
                indication = null,
                role = Role.Checkbox,
                enabled = enabled,
            ) {
                onValueChange(
                    when (value) {
                        RuiCheckboxState.On -> RuiCheckboxState.Off
                        RuiCheckboxState.Off, RuiCheckboxState.Indeterminate -> RuiCheckboxState.On
                    },
                )
            }
            .semantics {
                toggleableState = when (value) {
                    RuiCheckboxState.On -> ToggleableState.On
                    RuiCheckboxState.Off -> ToggleableState.Off
                    RuiCheckboxState.Indeterminate -> ToggleableState.Indeterminate
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(boxShape)
                .background(if (filled) colors.primary else Color.Transparent)
                .border(
                    width = dimens.borderWidth,
                    color = when {
                        filled -> Color.Transparent
                        enabled -> colors.outline
                        else -> colors.outlineSubtle
                    },
                    shape = boxShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = value == RuiCheckboxState.On,
                enter = fadeIn(motion.fast) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = motion.pop,
                ),
                exit = fadeOut(tween(durationMillis = 90)),
            ) {
                RuiIcon(
                    imageVector = RuiIcons.Check,
                    contentDescription = null,
                    size = 14.dp,
                    tint = colors.onPrimary,
                )
            }
            AnimatedVisibility(
                visible = value == RuiCheckboxState.Indeterminate,
                enter = fadeIn(motion.fast) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = motion.pop,
                ),
                exit = fadeOut(tween(durationMillis = 90)),
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .background(colors.onPrimary, RuiTheme.shapes.full),
                )
            }
        }
    }
}
