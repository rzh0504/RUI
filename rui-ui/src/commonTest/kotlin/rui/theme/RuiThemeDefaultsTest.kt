package rui.theme

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RuiThemeDefaultsTest {

    @Test
    fun lightSchemeSpecifiesEveryRole() {
        val colors = RuiColorScheme.light()
        val roles = listOf(
            colors.background,
            colors.surface,
            colors.surfaceMuted,
            colors.surfaceElevated,
            colors.outline,
            colors.outlineSubtle,
            colors.content,
            colors.contentMuted,
            colors.contentDisabled,
            colors.primary,
            colors.onPrimary,
            colors.danger,
            colors.success,
            colors.scrim,
        )
        roles.forEachIndexed { index, color ->
            assertTrue(color != Color.Unspecified, "Light role #$index is unspecified")
        }
    }

    @Test
    fun darkSchemeSpecifiesEveryRoleAndContrastsWithLight() {
        val dark = RuiColorScheme.dark()
        val light = RuiColorScheme.light()

        listOf(
            dark.background,
            dark.surface,
            dark.surfaceMuted,
            dark.surfaceElevated,
            dark.outline,
            dark.outlineSubtle,
            dark.content,
            dark.contentMuted,
            dark.contentDisabled,
            dark.primary,
            dark.onPrimary,
            dark.danger,
            dark.success,
            dark.scrim,
        ).forEachIndexed { index, color ->
            assertTrue(color != Color.Unspecified, "Dark role #$index is unspecified")
        }

        assertNotEquals(light.background, dark.background)
        assertNotEquals(light.content, dark.content)
    }

    @Test
    fun typographyDefinesAllNineStyles() {
        val typography = ruiTypography()
        val styles = listOf(
            typography.titleLg,
            typography.titleMd,
            typography.titleSm,
            typography.bodyLg,
            typography.bodyMd,
            typography.bodySm,
            typography.labelLg,
            typography.labelMd,
            typography.labelSm,
        )
        styles.forEachIndexed { index, style ->
            assertTrue(style.fontSize.value > 0f, "Typography style #$index has no font size")
            assertTrue(style.lineHeight.value >= style.fontSize.value, "Style #$index line height below font size")
        }
    }

    @Test
    fun defaultSpacingFollowsTheGrid() {
        val spacing = RuiSpacing()
        listOf(spacing.xs, spacing.sm, spacing.md, spacing.lg, spacing.xl, spacing.xxl).forEach {
            assertEquals(0f, it.value % 4f, "Spacing token $it off the 4dp grid")
        }
    }

    @Test
    fun injectedTokensRoundTripThroughTheAccessors() {
        val spacing = RuiSpacing().copy(md = 10.dp)
        val dimens = RuiDimens().copy(touchTarget = 48.dp)
        val motion = RuiMotion()
        val windowWidthClass = RuiWindowWidthClass.Expanded

        composeInTest {
            ProvideRuiThemeLocals(
                colorScheme = RuiColorScheme.light(),
                typography = ruiTypography(),
                shapes = ruiShapes(),
                strings = RuiStringsDefaults.English,
                spacing = spacing,
                dimens = dimens,
                motion = motion,
                windowWidthClass = windowWidthClass,
            ) {
                assertEquals(spacing, RuiTheme.spacing)
                assertEquals(dimens, RuiTheme.dimens)
                assertEquals(motion, RuiTheme.motion)
                assertEquals(windowWidthClass, RuiTheme.windowWidthClass)
            }
        }
    }
}

/**
 * Runs [content] through a one-shot composition over a sink applier — enough
 * runtime for providing and reading composition locals, no platform needed.
 * The recomposer runs so the initial composition is accepted; nothing in the
 * content awaits frames, so the run is cancelled as soon as it's applied.
 */
private fun composeInTest(content: @Composable () -> Unit) {
    runBlocking {
        val recomposer = Recomposer(EmptyCoroutineContext)
        val runner = launch { recomposer.runRecomposeAndApplyChanges() }
        try {
            val composition = Composition(SinkApplier(), recomposer)
            composition.setContent(content)
            composition.dispose()
        } finally {
            runner.cancel()
        }
    }
}

private class SinkApplier : AbstractApplier<Any?>(Unit) {
    override fun insertTopDown(index: Int, instance: Any?) {}
    override fun insertBottomUp(index: Int, instance: Any?) {}
    override fun remove(index: Int, count: Int) {}
    override fun move(from: Int, to: Int, count: Int) {}
    override fun onClear() {}
}
