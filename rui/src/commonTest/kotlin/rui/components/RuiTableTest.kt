package rui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/** Sort cycling of [RuiTable] headers and the column DSL's key resolution. */
class RuiTableTest {

    @Test
    fun firstTapOnAColumnStartsAscending() {
        assertEquals(
            RuiTableSort("name", RuiTableSortDirection.Ascending),
            nextTableSort(current = null, column = "name"),
        )
    }

    @Test
    fun tappingTheActiveColumnFlipsTheDirection() {
        val ascending = RuiTableSort("name", RuiTableSortDirection.Ascending)
        assertEquals(
            RuiTableSort("name", RuiTableSortDirection.Descending),
            nextTableSort(ascending, column = "name"),
        )
        assertEquals(
            ascending,
            nextTableSort(RuiTableSort("name", RuiTableSortDirection.Descending), column = "name"),
        )
    }

    @Test
    fun tappingAnotherColumnStartsItAscending() {
        assertEquals(
            RuiTableSort("year", RuiTableSortDirection.Ascending),
            nextTableSort(RuiTableSort("name", RuiTableSortDirection.Descending), column = "year"),
        )
    }

    @Test
    fun sortKeysCompareStructurally() {
        // Data-class keys behave like column identity; non-equal keys never match.
        assertEquals(
            RuiTableSort(Year(2024), RuiTableSortDirection.Descending),
            nextTableSort(RuiTableSort(Year(2024), RuiTableSortDirection.Ascending), column = Year(2024)),
        )
        assertEquals(
            RuiTableSort("b", RuiTableSortDirection.Ascending),
            nextTableSort(RuiTableSort("a", RuiTableSortDirection.Ascending), column = "b"),
        )
    }

    @Test
    fun columnKeysDefaultToTheirDeclarationIndex() {
        val scope = RuiTableScope<String>().apply {
            column(title = "Title") { }
            column(key = "plays", title = "Plays") { }
            column(title = "Artist") { }
        }
        assertEquals(listOf<Any>(0, "plays", 2), scope.columns.map { it.sortKey })
    }

    @Test
    fun headerInsetsAddContentAndRowPaddingInLtr() {
        assertEquals(
            RuiTableHeaderInsets(start = 20.dp, end = 6.dp),
            ruiTableHeaderInsets(
                contentPadding = PaddingValues(start = 12.dp, end = 4.dp),
                rowContentPadding = PaddingValues(start = 8.dp, end = 2.dp),
                layoutDirection = LayoutDirection.Ltr,
            ),
        )
    }

    @Test
    fun headerInsetsResolveAbsolutePaddingInLtr() {
        assertEquals(
            RuiTableHeaderInsets(start = 24.dp, end = 18.dp),
            ruiTableHeaderInsets(
                contentPadding = PaddingValues.Absolute(left = 8.dp, top = 0.dp, right = 2.dp, bottom = 0.dp),
                rowContentPadding = PaddingValues(horizontal = 16.dp),
                layoutDirection = LayoutDirection.Ltr,
            ),
        )
    }

    @Test
    fun headerInsetsResolveAbsolutePaddingInRtl() {
        assertEquals(
            RuiTableHeaderInsets(start = 18.dp, end = 24.dp),
            ruiTableHeaderInsets(
                contentPadding = PaddingValues.Absolute(left = 8.dp, top = 0.dp, right = 2.dp, bottom = 0.dp),
                rowContentPadding = PaddingValues(horizontal = 16.dp),
                layoutDirection = LayoutDirection.Rtl,
            ),
        )
    }
}

private data class Year(val value: Int)
