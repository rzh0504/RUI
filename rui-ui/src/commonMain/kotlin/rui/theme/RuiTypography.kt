package rui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale: three levels (title / body / label) in three sizes (Lg / Md / Sm),
 * plus the iOS-style large title ([titleXl]).
 *
 * The default family is the platform system font (Roboto on Android, SF on iOS),
 * keeping rendering native while the *scale* stays identical on every platform.
 */
@Immutable
public data class RuiTypography(
    public val titleXl: TextStyle,
    public val titleLg: TextStyle,
    public val titleMd: TextStyle,
    public val titleSm: TextStyle,
    public val bodyLg: TextStyle,
    public val bodyMd: TextStyle,
    public val bodySm: TextStyle,
    public val labelLg: TextStyle,
    public val labelMd: TextStyle,
    public val labelSm: TextStyle,
)

public fun ruiTypography(fontFamily: FontFamily = FontFamily.Default): RuiTypography {
    fun style(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
        fontFamily = fontFamily,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        fontWeight = weight,
    )

    return RuiTypography(
        titleXl = style(34, 41, FontWeight.Bold),
        titleLg = style(22, 28, FontWeight.SemiBold),
        titleMd = style(17, 24, FontWeight.SemiBold),
        titleSm = style(15, 20, FontWeight.SemiBold),
        bodyLg = style(16, 24, FontWeight.Normal),
        bodyMd = style(14, 20, FontWeight.Normal),
        bodySm = style(13, 18, FontWeight.Normal),
        labelLg = style(14, 18, FontWeight.Medium),
        labelMd = style(12, 16, FontWeight.Medium),
        labelSm = style(11, 14, FontWeight.Medium),
    )
}
