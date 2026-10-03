package rui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

/**
 * Motion tokens. Rui keeps motion restrained: quick, small, spring-based where
 * a gesture is involved. Component-specific springs live with the component;
 * these tokens cover the shared cases.
 */
@Immutable
public data class RuiMotion(
    /** Alpha fade-in of interaction state layers. */
    public val stateLayerIn: FiniteAnimationSpec<Float> = tween(durationMillis = 90),
    /** Alpha fade-out of interaction state layers — slower out than in. */
    public val stateLayerOut: FiniteAnimationSpec<Float> = tween(durationMillis = 180),
    /** Default short transition (fades, small scale changes). */
    public val fast: FiniteAnimationSpec<Float> = tween(durationMillis = 120),
    /** Default medium transition (overlays, indicator moves). */
    public val normal: FiniteAnimationSpec<Float> = tween(durationMillis = 200),
)
