package rui.components

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
}

private data class Year(val value: Int)
