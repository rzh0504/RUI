package rui.theme

import androidx.compose.ui.graphics.Color
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
}
