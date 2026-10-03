package rui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape

/**
 * Corner-radius tokens. Components pick a token by *role*, never by pixel value,
 * so the geometry language stays consistent and a future squircle implementation
 * can replace [RoundedCornerShape] in one place.
 */
@Immutable
public data class RuiShapes(
    /** Small controls: checkbox, small badge. */
    public val sm: Shape,
    /** Default control shape: buttons, inputs, switches. */
    public val md: Shape,
    /** Grouped containers: cards, sheets. */
    public val lg: Shape,
    /** Large floating containers: dialogs. */
    public val xl: Shape,
    /** Pill / fully-round controls. */
    public val full: Shape,
)

public fun ruiShapes(): RuiShapes = RuiShapes(
    sm = RoundedCornerShape(8),
    md = RoundedCornerShape(12),
    lg = RoundedCornerShape(16),
    xl = RoundedCornerShape(20),
    full = RoundedCornerShape(percent = 50),
)
