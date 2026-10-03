package rui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Corner-radius tokens, rendered as continuous ("squircle") corners — see
 * [SmoothCornerShape]. Components pick a token by *role*, never by pixel
 * value, and every rectangular surface at the same nesting level shares the
 * same token so radii never fight on one screen.
 */
@Immutable
public data class RuiShapes(
    /** Tiny controls: checkbox, badges, token swatches. */
    public val sm: Shape,
    /** The workhorse radius: buttons, inputs, cards — one tier, always matching. */
    public val md: Shape,
    /** Larger grouped containers: sheets, bottom surfaces. */
    public val lg: Shape,
    /** Large floating containers: dialogs. */
    public val xl: Shape,
    /** Pills and stadiums: switches, segmented tracks. */
    public val full: Shape,
)

public fun ruiShapes(): RuiShapes = RuiShapes(
    sm = SmoothCornerShape(6.dp),
    md = SmoothCornerShape(10.dp),
    lg = SmoothCornerShape(14.dp),
    xl = SmoothCornerShape(18.dp),
    full = SmoothCornerShape(100.dp),
)
