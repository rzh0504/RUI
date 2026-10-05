package rui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

/**
 * Motion tokens. Rui keeps motion restrained: quick, small, spring-based where
 * a gesture is involved. Duration tokens cover fades and state layers; spring
 * tokens cover the motion families — each component picks a family, never a
 * raw spec.
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
    /**
     * One leg of a breathing loop (skeleton bones) — reversed and repeated,
     * so a full pulse cycle takes twice this. A tween by design: loop
     * animations need a duration-based spec.
     */
    public val pulse: TweenSpec<Float> = tween(
        durationMillis = 600,
        easing = FastOutSlowInEasing,
    ),
    /**
     * One pass of a sweeping highlight (skeleton bones) — linear, restarted
     * from the start edge each cycle.
     */
    public val sweep: TweenSpec<Float> = tween(
        durationMillis = 1200,
        easing = LinearEasing,
    ),

    /**
     * Gesture tracking: a value follows the finger — stiff, near-critically
     * damped, no visible lag.
     */
    public val tracking: SpringSpec<Float> = spring(
        dampingRatio = 0.9f,
        stiffness = 1755f,
    ),
    /**
     * Settle: the soft catch-up after a gesture ends or a programmatic value
     * change.
     */
    public val settle: SpringSpec<Float> = spring(
        dampingRatio = 0.96f,
        stiffness = 322f,
    ),
    /**
     * Press emphasis: small transforms that may overshoot slightly (a thumb
     * scaling up under the finger).
     */
    public val press: SpringSpec<Float> = spring(
        dampingRatio = 0.6f,
        stiffness = 987f,
    ),
    /**
     * Pop: small values and small surfaces moving with one soft overshoot —
     * a check mark drawing in, a switch thumb sliding, a dialog or menu
     * scaling in.
     */
    public val pop: SpringSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 450f,
    ),
    /**
     * Navigation: screen-scale moves — critically damped, no bounce at page
     * size.
     */
    public val navigation: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 550f,
    ),
)
