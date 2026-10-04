package rui.foundation

import androidx.compose.runtime.Immutable

/**
 * A two-point linear gradient describing how a progressive blur ramps from
 * full strength at [startFraction] to zero at [endFraction], measured along
 * [angle]. Use the [Top] / [Bottom] / [Left] / [Right] presets for the common
 * edge-fade cases.
 *
 * The descriptor is adapted from the miuix project's blur module (Apache
 * License 2.0, see NOTICE); Rui renders it as a layered cross-fade of blur
 * levels rather than a per-pixel runtime shader.
 *
 * @param angle Gradient direction in degrees, clockwise from the +X axis:
 *   `0` fades left→right, `90` top→bottom, `180` right→left, `270`
 *   bottom→top.
 * @param startFraction Fraction in `[0, 1]` of the surface's projected extent
 *   along [angle] where the blur is at full strength.
 * @param endFraction Fraction in `[0, 1]` where the blur fades to zero. May
 *   be smaller than [startFraction] to reverse the ramp; must differ from it.
 * @param curve Power-curve exponent reshaping the falloff between the fixed
 *   endpoints: `< 1` concentrates the radius change toward the strong end,
 *   `> 1` toward the clear end. Must be positive.
 */
@Immutable
public data class ProgressiveBlur(
    public val angle: Float = 90f,
    public val startFraction: Float = 0f,
    public val endFraction: Float = 1f,
    public val curve: Float = 1f,
) {
    public companion object {
        /** Blur strongest at the top edge, fading to clear at the bottom. */
        public val Top: ProgressiveBlur = ProgressiveBlur(angle = 90f, startFraction = 0f, endFraction = 1f)

        /** Blur strongest at the bottom edge, fading to clear at the top. */
        public val Bottom: ProgressiveBlur = ProgressiveBlur(angle = 270f, startFraction = 0f, endFraction = 1f)

        /** Blur strongest at the left edge, fading to clear at the right. */
        public val Left: ProgressiveBlur = ProgressiveBlur(angle = 0f, startFraction = 0f, endFraction = 1f)

        /** Blur strongest at the right edge, fading to clear at the left. */
        public val Right: ProgressiveBlur = ProgressiveBlur(angle = 180f, startFraction = 0f, endFraction = 1f)
    }
}
