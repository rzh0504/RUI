package rui.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.Thumb
import com.composeunstyled.Track
import com.composeunstyled.UnstyledSwitch
import rui.theme.RuiTheme

/**
 * The Rui switch: Unstyled's accessible toggle with a neutral track and a
 * perfectly round thumb in a circular track (no corner smoothing here —
 * switches read as pure circles).
 */
@Composable
public fun RuiSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    val trackColor = when {
        !enabled -> colors.surfaceMuted
        checked -> colors.primary
        else -> colors.surfaceMuted
    }
    val thumbColor = when {
        !enabled -> colors.contentDisabled
        checked -> colors.onPrimary
        else -> colors.surface
    }

    UnstyledSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        interactionSource = resolvedInteractionSource,
        content = {
            Track(
                modifier = Modifier
                    .size(width = 40.dp, height = 24.dp)
                    .clip(CircleShape)
                    .background(trackColor)
                    .border(
                        width = dimens.borderWidth,
                        color = if (checked) Color.Transparent else colors.outline,
                        shape = CircleShape,
                    ),
            ) {
                Thumb(
                    modifier = Modifier.padding(3.dp),
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(thumbColor),
                    )
                }
            }
        },
    )
}
