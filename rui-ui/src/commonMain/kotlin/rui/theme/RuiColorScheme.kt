package rui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color roles of the Rui design system.
 *
 * Roles describe *what a color is for*, never what brand it comes from, so a
 * neutral look is the default and a brand identity is a single [copy] away:
 *
 * ```
 * RuiTheme(colorScheme = RuiColorScheme.light().copy(primary = Color(0xFF3B6EF2))) { ... }
 * ```
 */
@Immutable
public data class RuiColorScheme(
    /** Page background behind everything. */
    public val background: Color,
    /** Card / sheet / input container resting on the background. */
    public val surface: Color,
    /** Muted, non-elevated container (list headers, wells, code blocks). */
    public val surfaceMuted: Color,
    /** Floating container above content (menus, dialogs without glass). */
    public val surfaceElevated: Color,
    /** Strong hairline separating a container from its surroundings. */
    public val outline: Color,
    /** Faint hairline for internal dividers and restful borders. */
    public val outlineSubtle: Color,
    /** Primary text and icon color on [surface]. */
    public val content: Color,
    /** Secondary text: helper text, captions, inactive labels. */
    public val contentMuted: Color,
    /** Text/icons of disabled components. */
    public val contentDisabled: Color,
    /** Accent color for emphasis, selection and primary actions. */
    public val primary: Color,
    /** Content drawn on top of [primary]. */
    public val onPrimary: Color,
    /** Destructive actions and error states. */
    public val danger: Color,
    /** Affirmative feedback. */
    public val success: Color,
    /** Overlay drawn between content and a modal fragment. */
    public val scrim: Color,
) {
    public companion object {
        public fun light(): RuiColorScheme = RuiColorScheme(
            background = Color(0xFFF7F7F8),
            surface = Color(0xFFFFFFFF),
            surfaceMuted = Color(0xFFF1F1F3),
            surfaceElevated = Color(0xFFFFFFFF),
            outline = Color(0xFFDEDEE2),
            outlineSubtle = Color(0xFFE9E9EC),
            content = Color(0xFF1A1A1F),
            contentMuted = Color(0xFF70707A),
            contentDisabled = Color(0xFFB0B0B8),
            primary = Color(0xFF222227),
            onPrimary = Color(0xFFFFFFFF),
            danger = Color(0xFFDC3D43),
            success = Color(0xFF2E9E5B),
            scrim = Color(0x99000000),
        )

        public fun dark(): RuiColorScheme = RuiColorScheme(
            background = Color(0xFF0F0F12),
            surface = Color(0xFF17171B),
            surfaceMuted = Color(0xFF212126),
            surfaceElevated = Color(0xFF1D1D22),
            outline = Color(0xFF33333A),
            outlineSubtle = Color(0xFF29292F),
            content = Color(0xFFEDEDF1),
            contentMuted = Color(0xFF9B9BA5),
            contentDisabled = Color(0xFF565660),
            primary = Color(0xFFEDEDF1),
            onPrimary = Color(0xFF1A1A1F),
            danger = Color(0xFFE5484D),
            success = Color(0xFF46A758),
            scrim = Color(0x99000000),
        )
    }
}
