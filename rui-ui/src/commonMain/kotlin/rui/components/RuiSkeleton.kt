package rui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import rui.theme.RuiTheme

/** How far a bone breathes down from full opacity. */
private val PulseMinAlpha = 0.4f

/**
 * One bone of a loading skeleton: a muted, breathing block standing in for
 * content that is on its way. Size it with [modifier] — a bone without a
 * height constraint gets one line of body text; give it `CircleShape` to
 * stand in for an avatar. Bones are invisible to accessibility; announce the
 * loading state on the container that composes them.
 *
 * Compose bones into the shapes of the missing content: an avatar circle,
 * title and subtitle lines at the typography's line heights, paragraph
 * blocks. Swap the composed block for the real content when it arrives.
 *
 * @param shape Bone shape; `RuiTheme.shapes.sm` reads as a text line,
 *   `CircleShape` as an avatar.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun RuiSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RuiTheme.shapes.sm,
) {
    val colors = RuiTheme.colors
    val motion = RuiTheme.motion
    val lineHeight = with(LocalDensity.current) {
        RuiTheme.typography.bodyMd.lineHeight.toDp()
    }

    val pulse = rememberInfiniteTransition(label = "RuiSkeleton")
    val alpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = PulseMinAlpha,
        animationSpec = infiniteRepeatable(
            animation = motion.pulse,
            repeatMode = RepeatMode.Reverse,
        ),
        label = "RuiSkeletonAlpha",
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = lineHeight)
            .alpha(alpha)
            .background(colors.surfaceMuted, shape)
            .semantics { invisibleToUser() },
    )
}
