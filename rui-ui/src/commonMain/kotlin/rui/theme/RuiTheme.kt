package rui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.composeunstyled.theme.buildThemeV2
import com.composeunstyled.theme.rememberColoredIndication

private val LocalRuiColorScheme = staticCompositionLocalOf { RuiColorScheme.light() }
private val LocalRuiTypography = staticCompositionLocalOf { ruiTypography() }
private val LocalRuiShapes = staticCompositionLocalOf { ruiShapes() }
private val LocalRuiSpacing = staticCompositionLocalOf { RuiSpacing() }
private val LocalRuiDimens = staticCompositionLocalOf { RuiDimens() }
private val LocalRuiMotion = staticCompositionLocalOf { RuiMotion() }

/**
 * Applies the Rui design system to [content].
 *
 * Besides Rui's own tokens, this bridges the resolved values into Compose
 * Unstyled's theme (content color, default text style, selection colors and the
 * state-layer indication), so Unstyled primitives composed without a Rui wrapper
 * still render consistently.
 *
 * Every token set is injectable. [spacing], [dimens] and [motion] are data
 * classes, so a whole-family adjustment is a [RuiSpacing.copy]/[RuiDimens.copy]/
 * [RuiMotion.copy] away — e.g. a denser layout, or larger spacing for an
 * accessibility preset:
 *
 * ```
 * RuiTheme(spacing = RuiSpacing().copy(md = 8.dp, lg = 12.dp)) { … }
 * ```
 */
public object RuiTheme {
    @Composable
    public operator fun invoke(
        darkTheme: Boolean = isSystemInDarkTheme(),
        colorScheme: RuiColorScheme = if (darkTheme) RuiColorScheme.dark() else RuiColorScheme.light(),
        typography: RuiTypography = ruiTypography(),
        shapes: RuiShapes = ruiShapes(),
        strings: RuiStrings = RuiStringsDefaults.English,
        spacing: RuiSpacing = RuiSpacing(),
        dimens: RuiDimens = RuiDimens(),
        motion: RuiMotion = RuiMotion(),
        content: @Composable () -> Unit,
    ) {
        val unstyledTheme = buildThemeV2 {
            defaultIndication = rememberColoredIndication(
                color = colorScheme.content,
                pressedAlpha = StateLayerAlphas.pressed,
                hoveredAlpha = StateLayerAlphas.hovered,
                focusedAlpha = StateLayerAlphas.focused,
                draggedAlpha = StateLayerAlphas.dragged,
                showAnimationSpec = motion.stateLayerIn,
                hideAnimationSpec = motion.stateLayerOut,
            )
            defaultContentColor = colorScheme.content
            defaultTextStyle = typography.bodyMd
            defaultTextSelectionColors = TextSelectionColors(
                handleColor = colorScheme.primary,
                backgroundColor = colorScheme.primary.copy(alpha = 0.2f),
            )
        }

        ProvideRuiThemeLocals(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            strings = strings,
            spacing = spacing,
            dimens = dimens,
            motion = motion,
        ) {
            unstyledTheme(content)
        }
    }

    public val colors: RuiColorScheme
        @Composable get() = LocalRuiColorScheme.current

    public val typography: RuiTypography
        @Composable get() = LocalRuiTypography.current

    public val shapes: RuiShapes
        @Composable get() = LocalRuiShapes.current

    public val strings: RuiStrings
        @Composable get() = LocalRuiStrings.current

    public val spacing: RuiSpacing
        @Composable get() = LocalRuiSpacing.current

    public val dimens: RuiDimens
        @Composable get() = LocalRuiDimens.current

    public val motion: RuiMotion
        @Composable get() = LocalRuiMotion.current
}

/** Alphas of the content-colored state layer applied to interactive components. */
internal object StateLayerAlphas {
    const val pressed = 0.10f
    const val hovered = 0.06f
    const val focused = 0.08f
    const val dragged = 0.12f
}

/**
 * Wires [RuiTheme]'s resolved token sets into the composition locals its
 * accessors read. Kept separate from [RuiTheme.invoke] so tests can verify the
 * parameter-to-local wiring without composing the Unstyled theme bridge, which
 * resolves the system theme from platform locals a unit test has none of.
 */
@Composable
internal fun ProvideRuiThemeLocals(
    colorScheme: RuiColorScheme,
    typography: RuiTypography,
    shapes: RuiShapes,
    strings: RuiStrings,
    spacing: RuiSpacing,
    dimens: RuiDimens,
    motion: RuiMotion,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalRuiColorScheme provides colorScheme,
        LocalRuiTypography provides typography,
        LocalRuiShapes provides shapes,
        LocalRuiStrings provides strings,
        LocalRuiSpacing provides spacing,
        LocalRuiDimens provides dimens,
        LocalRuiMotion provides motion,
        content = content,
    )
}
