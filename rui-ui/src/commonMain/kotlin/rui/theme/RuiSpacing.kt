package rui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing tokens on a 4dp grid. */
@Immutable
public data class RuiSpacing(
    public val xs: Dp = 4.dp,
    public val sm: Dp = 8.dp,
    public val md: Dp = 12.dp,
    public val lg: Dp = 16.dp,
    public val xl: Dp = 24.dp,
    public val xxl: Dp = 32.dp,
)

/** Metric tokens shared across components. */
@Immutable
public data class RuiDimens(
    /** Minimum interactive target, balancing Material (48dp) and HIG (44dp) guidance. */
    public val touchTarget: Dp = 44.dp,
    public val borderWidth: Dp = 1.dp,
    public val focusRingWidth: Dp = 2.dp,
    public val iconSize: Dp = 20.dp,
    /** Height of one row in a wheel picker drum, iOS HIG proportion. */
    public val pickerRowHeight: Dp = 36.dp,
)
