package rui.foundation.utils

import androidx.compose.animation.core.Easing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The step response of an underdamped spring baked into an [Easing], played
 * over a fixed tween duration.
 *
 * Baking the spring solution into an easing (instead of running a live
 * spring) pins the total duration: a live spring's settle time scales with
 * distance and termination threshold, while a tween always completes in
 * exactly its configured duration — which is what the navigation stack
 * driver needs, since its end gates structural changes (render-stack
 * removal) and backdrop recording. The shipped parameters give a brisk
 * middle and a long, gentle tail that lands sub-perceptually short of the
 * target (transform(1f) ≈ 0.9993, monotone within 0..1); the tween's final
 * frame snaps the residue — a fraction of a pixel over a screen-width
 * travel, invisible.
 *
 * Adapted from miuix-nav's NavSettleEasing
 * (https://github.com/composable-miuix-ui/miuix, Apache License 2.0); see
 * NOTICE.
 *
 * @param response oscillation period of the underlying spring, in units of
 *   the played duration.
 * @param damping damping ratio of the underlying spring (`< 1`, underdamped).
 */
internal class NavSettleEasing(
    response: Float,
    damping: Float,
) : Easing {
    private val r: Float
    private val w: Float
    private val c2: Float

    init {
        val omega = 2.0 * PI / response
        val k = omega * omega
        val c = damping * 4.0 * PI / response

        w = (sqrt(4.0 * k - c * c) / 2.0).toFloat()
        r = (-c / 2.0).toFloat()
        c2 = r / w
    }

    override fun transform(fraction: Float): Float {
        val t = fraction.toDouble()
        val decay = exp(r * t)
        return (decay * (-cos(w * t) + c2 * sin(w * t)) + 1.0).toFloat()
    }
}
