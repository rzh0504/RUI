package rui.foundation.utils

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Sinks the element slightly while its [interactionSource] reports a press —
 * the Rui take on press feedback that pairs with (or replaces) the state
 * layer for cards, tiles and other large surfaces.
 *
 * ```kotlin
 * val interactionSource = remember { MutableInteractionSource() }
 * Box(
 *     Modifier
 *         .pressScale(interactionSource)
 *         .clickable(interactionSource, null) { … },
 * )
 * ```
 */
@Composable
public fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
    animationSpec: SpringSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 600f),
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = animationSpec,
        label = "pressScale",
    )

    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
