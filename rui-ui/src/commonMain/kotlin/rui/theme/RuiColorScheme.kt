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
            background = Color(0xFFF2F2F6),
            surface = Color(0xFFFFFFFF),
            surfaceMuted = Color(0xFFE9E9EE),
            surfaceElevated = Color(0xFFFFFFFF),
            outline = Color(0xFFD9D9DE),
            outlineSubtle = Color(0xFFE3E3E8),
            content = Color(0xFF1C1C21),
            contentMuted = Color(0xFF71717B),
            contentDisabled = Color(0xFFB4B4BC),
            primary = Color(0xFF3C87FF),
            onPrimary = Color(0xFFFFFFFF),
            danger = Color(0xFFDC3D43),
            success = Color(0xFF2E9E5B),
            scrim = Color(0x73000000),
        )

        public fun dark(): RuiColorScheme = RuiColorScheme(
            background = Color(0xFF0D0D10),
            surface = Color(0xFF1A1A1F),
            surfaceMuted = Color(0xFF26262C),
            surfaceElevated = Color(0xFF232329),
            outline = Color(0xFF3B3B42),
            outlineSubtle = Color(0xFF2E2E35),
            content = Color(0xFFF0F0F4),
            contentMuted = Color(0xFF9B9BA5),
            contentDisabled = Color(0xFF5A5A64),
            primary = Color(0xFF3C87FF),
            onPrimary = Color(0xFFFFFFFF),
            danger = Color(0xFFE5484D),
            success = Color(0xFF46A758),
            scrim = Color(0x73000000),
        )
    }
}
