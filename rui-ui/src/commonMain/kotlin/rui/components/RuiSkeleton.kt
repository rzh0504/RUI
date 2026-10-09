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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import rui.theme.RuiTheme

/** How far a breathing bone sinks from full opacity. */
private val PulseMinAlpha = 0.4f

/** Width of the sweeping highlight band, as a fraction of the bone. */
private const val SweepBandFraction = 0.6f

/** The sheen over the muted base — a white highlight reads on light and dark. */
private val SweepHighlight = Color.White.copy(alpha = 0.55f)

/** The looping animation of a skeleton bone. */
public enum class RuiSkeletonEffect {
    /** The whole bone breathes between full and muted opacity. */
    Pulse,

    /** A highlight band sweeps across the bone, start edge to end edge. */
    Sweep,
}

/**
 * One bone of a loading skeleton: a muted block standing in for content that
 * is on its way, animated by [effect] — a breathing pulse (default) or a
 * highlight sweeping left to right. Size it with [modifier] — a bone without
 * a height constraint gets one line of body text; give it `CircleShape` to
 * stand in for an avatar. Bones are invisible to accessibility; announce the
 * loading state on the container that composes them.
 *
 * Compose bones into the shapes of the missing content: an avatar circle,
 * title and subtitle lines at the typography's line heights, paragraph
 * blocks. Swap the composed block for the real content when it arrives.
 *
 * @param shape Bone shape; `RuiTheme.shapes.sm` reads as a text line,
 *   `CircleShape` as an avatar.
 * @param effect The looping animation; one per screen's skeletons for a
 *   coherent load.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun RuiSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = RuiTheme.shapes.sm,
    effect: RuiSkeletonEffect = RuiSkeletonEffect.Pulse,
) {
    val colors = RuiTheme.colors
    val motion = RuiTheme.motion
    val lineHeight = with(LocalDensity.current) {
        RuiTheme.typography.bodyMd.lineHeight.toDp()
    }
    val bone = modifier
        .defaultMinSize(minHeight = lineHeight)
        .semantics { hideFromAccessibility() }

    when (effect) {
        RuiSkeletonEffect.Pulse -> {
            val pulse = rememberInfiniteTransition(label = "RuiSkeletonPulse")
            val alpha by pulse.animateFloat(
                initialValue = 1f,
                targetValue = PulseMinAlpha,
                animationSpec = infiniteRepeatable(
                    animation = motion.pulse,
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "RuiSkeletonPulseAlpha",
            )
            Box(modifier = bone.alpha(alpha).background(colors.surfaceMuted, shape))
        }

        RuiSkeletonEffect.Sweep -> {
            val sweep = rememberInfiniteTransition(label = "RuiSkeletonSweep")
            val position by sweep.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(animation = motion.sweep),
                label = "RuiSkeletonSweepPosition",
            )
            Box(
                modifier = bone
                    .clip(shape)
                    .background(colors.surfaceMuted)
                    .drawBehind {
                        // The band travels from fully before the start edge to
                        // fully past the end edge; mirrored in RTL.
                        val traveled = if (layoutDirection == LayoutDirection.Rtl) {
                            1f - position
                        } else {
                            position
                        }
                        val bandWidth = size.width * SweepBandFraction
                        val center = (size.width + bandWidth) * traveled - bandWidth / 2f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    SweepHighlight,
                                    Color.Transparent,
                                ),
                                start = Offset(center - bandWidth / 2f, 0f),
                                end = Offset(center + bandWidth / 2f, 0f),
                            ),
                        )
                    },
            )
        }
    }
}
