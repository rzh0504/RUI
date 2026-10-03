package rui.components

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composeunstyled.CheckedIndicator
import com.composeunstyled.UnstyledCheckbox
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.theme.RuiTheme

/**
 * The Rui checkbox: a 20dp square that fills with the primary color and shows a
 * check mark when selected. The visual mark animates in; the hit area covers
 * the theme's touch target.
 */
@Composable
public fun RuiCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val motion = RuiTheme.motion
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    UnstyledCheckbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.size(dimens.touchTarget),
        enabled = enabled,
        interactionSource = resolvedInteractionSource,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val boxShape = RuiTheme.shapes.sm

            CheckedIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .clip(boxShape)
                    .background(if (checked) colors.primary else Color.Transparent)
                    .border(
                        width = dimens.borderWidth,
                        color = when {
                            checked -> Color.Transparent
                            enabled -> colors.outline
                            else -> colors.outlineSubtle
                        },
                        shape = boxShape,
                    ),
                enter = fadeIn(motion.fast) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 600f),
                ),
                exit = fadeOut(tween(durationMillis = 90)),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    RuiIcon(
                        imageVector = RuiIcons.Check,
                        contentDescription = null,
                        size = 14.dp,
                        tint = colors.onPrimary,
                    )
                }
            }
        }
    }
}
