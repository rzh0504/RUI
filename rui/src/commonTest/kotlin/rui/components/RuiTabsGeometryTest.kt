package rui.components

import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals

/** The selection pill's physical offset from a TopStart anchor, mirrored once for RTL. */
class RuiTabsGeometryTest {

    private val trackWidth = 300f
    private val pillWidth = 80f

    @Test
    fun ltrUsesThePhysicalLeft() {
        for (left in listOf(0f, 110f, 220f)) {
            assertEquals(
                left,
                pillOffsetX(left, trackWidth, pillWidth, LayoutDirection.Ltr),
            )
        }
    }

    @Test
    fun rtlMirrorsBackFromTheStartEdge() {
        // TopStart sits on the physical right in RTL; the offset must still land on the physical left.
        val start = trackWidth - pillWidth
        for (left in listOf(0f, 110f, 220f)) {
            val offset = pillOffsetX(left, trackWidth, pillWidth, LayoutDirection.Rtl)
            assertEquals(left, start + offset)
        }
    }

    @Test
    fun rtlPinsBothTrackEnds() {
        assertEquals(-220f, pillOffsetX(0f, trackWidth, pillWidth, LayoutDirection.Rtl))
        assertEquals(0f, pillOffsetX(220f, trackWidth, pillWidth, LayoutDirection.Rtl), 0f)
    }

    @Test
    fun rtlReadsTheAnimatedPillWidth() {
        val offset = pillOffsetX(110f, trackWidth, pillWidth = 100f, LayoutDirection.Rtl)
        assertEquals(-90f, offset)
        assertEquals(110f, trackWidth - 100f + offset)
    }
}
