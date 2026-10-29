package rui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Corner-radius tokens, mostly rendered as continuous ("squircle") corners —
 * see [SmoothCornerShape]; [RuiShapes.full] is the exception, a plain capsule.
 * Components pick a token by *role*, never by pixel value, and every
 * rectangular surface at the same nesting level shares the same token so
 * radii never fight on one screen.
 */
@Immutable
public data class RuiShapes(
    /** Tiny controls: checkbox, badges, token swatches. */
    public val sm: Shape,
    /** The workhorse radius: buttons, inputs, cards — one tier, always matching. */
    public val md: Shape,
    /** Larger grouped containers: sheets, bottom surfaces. */
    public val lg: Shape,
    /** Surfaces docked to the screen's bottom edge (sheets): rounded at the top, square at the edge. */
    public val sheet: Shape,
    /** Large floating containers: dialogs. */
    public val xl: Shape,
    /**
     * Pills and stadiums: switches, segmented tracks. A plain (unsmoothed)
     * stadium — squircle smoothing is illegible at this curvature, so pills
     * read as true capsules.
     */
    public val full: Shape,
)

public fun ruiShapes(): RuiShapes = RuiShapes(
    sm = SmoothCornerShape(6.dp),
    md = SmoothCornerShape(10.dp),
    lg = SmoothCornerShape(14.dp),
    sheet = TopSmoothCornerShape(14.dp),
    xl = SmoothCornerShape(18.dp),
    full = RoundedCornerShape(percent = 50),
)
