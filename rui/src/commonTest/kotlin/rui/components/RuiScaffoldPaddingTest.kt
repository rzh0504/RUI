package rui.components

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/** Scaffold content padding keeps window insets on their physical sides in both directions. */
class RuiScaffoldPaddingTest {

    private val padding = scaffoldContentPadding(top = 8.dp, bottom = 4.dp, left = 10.dp, right = 20.dp)

    @Test
    fun ltrStartIsThePhysicalLeft() {
        assertEquals(10.dp, padding.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(20.dp, padding.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(10.dp, padding.calculateStartPadding(LayoutDirection.Ltr))
        assertEquals(20.dp, padding.calculateEndPadding(LayoutDirection.Ltr))
        assertEquals(8.dp, padding.calculateTopPadding())
        assertEquals(4.dp, padding.calculateBottomPadding())
    }

    @Test
    fun rtlStartIsThePhysicalRight() {
        assertEquals(10.dp, padding.calculateLeftPadding(LayoutDirection.Rtl))
        assertEquals(20.dp, padding.calculateRightPadding(LayoutDirection.Rtl))
        assertEquals(20.dp, padding.calculateStartPadding(LayoutDirection.Rtl))
        assertEquals(10.dp, padding.calculateEndPadding(LayoutDirection.Rtl))
    }
}
