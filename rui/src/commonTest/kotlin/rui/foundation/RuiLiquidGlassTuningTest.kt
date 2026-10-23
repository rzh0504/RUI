package rui.foundation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The tuning values feed a shader pipeline — an alpha above 1, a negative
 * blur or a NaN multiplier has no meaningful rendering. They are rejected at
 * construction so a bad preset fails at the call site instead of producing a
 * broken frame.
 */
class RuiLiquidGlassTuningTest {

    @Test
    fun defaultsAndBoundariesAreAccepted() {
        RuiLiquidGlassTuning()
        RuiLiquidGlassTuning(tintAlpha = 0f, highlightAlpha = 1f, saturation = 0f)
        RuiLiquidGlassTuning(blurRadius = 0.dp, refractionHeight = 0.dp)
    }

    @Test
    fun tintAlphaOutsideUnitRangeIsRejected() {
        assertFailsWith<IllegalArgumentException> { RuiLiquidGlassTuning(tintAlpha = 1.1f) }
        assertFailsWith<IllegalArgumentException> { RuiLiquidGlassTuning(tintAlpha = -0.1f) }
        assertFailsWith<IllegalArgumentException> { RuiLiquidGlassTuning(tintAlpha = Float.NaN) }
    }

    @Test
    fun highlightAlphaOutsideUnitRangeIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            RuiLiquidGlassTuning(highlightAlpha = 1.1f)
        }
        assertFailsWith<IllegalArgumentException> {
            RuiLiquidGlassTuning(highlightAlpha = Float.NaN)
        }
    }

    @Test
    fun negativeSizesAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            RuiLiquidGlassTuning(blurRadius = (-1).dp)
        }
        assertFailsWith<IllegalArgumentException> {
            RuiLiquidGlassTuning(blurRadius = Dp.Unspecified)
        }
        assertFailsWith<IllegalArgumentException> {
            RuiLiquidGlassTuning(refractionHeight = (-1).dp)
        }
    }

    @Test
    fun negativeOrNaNSaturationIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            RuiLiquidGlassTuning(saturation = -1f)
        }
        assertFailsWith<IllegalArgumentException> {
            RuiLiquidGlassTuning(saturation = Float.NaN)
        }
    }
}
