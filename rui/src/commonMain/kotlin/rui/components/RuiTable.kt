package rui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiTheme

/**
 * Width policy of one [RuiTable] column.
 */
public sealed interface RuiTableColumnWidth {

    /** A column exactly [value] wide; content wider than it wraps inside. */
    public data class Fixed(val value: Dp) : RuiTableColumnWidth

    /**
     * A column sharing the table's leftover width by [weight], with
     * [androidx.compose.foundation.layout.RowScope.weight] semantics — at
     * least one weighted column is what makes a table stretch with its
     * container.
     */
    public data class Weighted(val weight: Float) : RuiTableColumnWidth
}

/** Direction of the active [RuiTableSort]. */
public enum class RuiTableSortDirection { Ascending, Descending }

/**
 * The active sort of a [RuiTable]: which column (by its [RuiTableScope.column]
 * key) and which direction. The table never touches the data — sorting the
 * items remains the caller's job, so any backend (an in-memory list, a paged
 * source) can drive the same headers.
 */
public data class RuiTableSort(
    public val column: Any,
    public val direction: RuiTableSortDirection,
)

/** The sort a tap on [column] produces given the [current] one: a new column
 *  starts ascending, the active column flips. */
internal fun nextTableSort(current: RuiTableSort?, column: Any): RuiTableSort =
    if (current?.column == column && current.direction == RuiTableSortDirection.Ascending) {
        RuiTableSort(column, RuiTableSortDirection.Descending)
    } else {
        RuiTableSort(column, RuiTableSortDirection.Ascending)
    }

/** Total horizontal inset of the header: the viewport [contentPadding] plus [rowContentPadding]. */
internal data class RuiTableHeaderInsets(val start: Dp, val end: Dp)

internal fun ruiTableHeaderInsets(
    contentPadding: PaddingValues,
    rowContentPadding: PaddingValues,
    layoutDirection: LayoutDirection,
): RuiTableHeaderInsets = RuiTableHeaderInsets(
    start = contentPadding.calculateStartPadding(layoutDirection) +
        rowContentPadding.calculateStartPadding(layoutDirection),
    end = contentPadding.calculateEndPadding(layoutDirection) +
        rowContentPadding.calculateEndPadding(layoutDirection),
)

/**
 * Colors of [RuiTable]. Cells render their own content and read the ambient
 * theme themselves, so only the chrome the table draws — the header and the
 * hairlines — is colored here.
 */
@Immutable
public data class RuiTableColors(
    /** Column header text and sort glyphs. */
    public val headerContent: Color,
    /** Header and row hairlines. */
    public val divider: Color,
)

/** Default values for [RuiTable]. */
public object RuiTableDefaults {

    @Composable
    public fun colors(
        headerContent: Color = RuiTheme.colors.contentMuted,
        divider: Color = RuiTheme.colors.outlineSubtle,
    ): RuiTableColors = RuiTableColors(
        headerContent = headerContent,
        divider = divider,
    )
}

internal class RuiTableColumn<T>(
    val sortKey: Any,
    val width: RuiTableColumnWidth,
    val alignment: Alignment.Horizontal,
    val title: String?,
    val header: (@Composable () -> Unit)?,
    val sortable: Boolean,
    val cell: @Composable (T) -> Unit,
)

/**
 * The column DSL of [RuiTable]. Columns are declared in order; every row lays
 * its cells out in that order.
 */
public class RuiTableScope<T> internal constructor() {

    internal val columns = mutableListOf<RuiTableColumn<T>>()

    /**
     * Declares one column.
     *
     * @param key Identity used by sorting; defaults to the declaration index.
     *   Give a stable one when columns can be reordered between compositions.
     * @param width [RuiTableColumnWidth.Fixed] or
     *   [RuiTableColumnWidth.Weighted]; defaults to a weight of 1.
     * @param alignment Horizontal alignment of both the header and the cells.
     * @param title Header text rendered in the header's default style. The
     *   [header] slot wins when both are given.
     * @param header Custom header content — an icon beside the title, a
     *   filter affordance. Sortability is independent of it: the tap target
     *   is always the whole cell.
     * @param sortable Makes the header tappable and, when it is the active
     *   sort, shows the direction glyph.
     * @param cell Content of one cell of this column for one row's item.
     */
    public fun column(
        key: Any? = null,
        width: RuiTableColumnWidth = RuiTableColumnWidth.Weighted(1f),
        alignment: Alignment.Horizontal = Alignment.Start,
        title: String? = null,
        header: (@Composable () -> Unit)? = null,
        sortable: Boolean = false,
        cell: @Composable (item: T) -> Unit,
    ) {
        columns += RuiTableColumn(
            sortKey = key ?: columns.size,
            width = width,
            alignment = alignment,
            title = title,
            header = header,
            sortable = sortable,
            cell = cell,
        )
    }
}

/**
 * A data table: a pinned header row above virtualized rows, shaped entirely
 * by the caller's columns.
 *
 * The [content] DSL declares one [RuiTableScope.column] per column — its
 * width policy, its alignment, and a composable slot per cell, so anything a
 * composable can render can sit in a cell; text is just the common case. The
 * header follows suit: a column renders its `title` in the header style by
 * default and its [RuiTableScope.header] slot instead when one is given.
 *
 * Columns are the extension surface and everything else stays out of the way:
 * rows are transparent by default (a [rui.foundation.RuiSurface] card, like
 * the grouped lists, is the caller's choice), a [rowBackground] hook colors
 * any row (zebra stripes, selection, a conditional tint), [onRowClick] makes
 * rows tappable, and [RuiTableColors] re-colors the chrome the table itself
 * draws.
 *
 * Sortable columns hoist their state: pass [sort] and [onSortChange], sort
 * the items yourself before handing them in — the table never reorders data,
 * which keeps paged or lazily-loaded backends first-class — and the header
 * renders the direction glyph with sorted-state semantics. Tapping the active
 * column flips the direction; tapping another column starts it ascending.
 *
 * The table is a scrolling surface: it fills the height it is given, its rows
 * virtualize like [RuiLazyColumn]'s, and [contentPadding] follows the
 * scaffold convention (viewport padding, so rows scroll under glass bars).
 * An empty [count] renders the header over nothing — overlay a
 * [RuiEmptyState] when the no-rows moment needs one.
 *
 * @param count Number of rows.
 * @param itemAt The row's item for an index; called while rows compose.
 * @param key Identity of a row for reuse and reordering; defaults to the row
 *   index. Must return distinct values, as on [LazyColumn].
 * @param contentPadding Viewport padding of the scrolling rows; its horizontal
 *   part also insets the header so columns stay aligned.
 * @param rowContentPadding Padding wrapping every header and row (and
 *   insetting the hairlines) — the table's breathing room inside its
 *   container.
 * @param showHeader Renders the header row. Turn off for a borderless grid
 *   used purely for layout.
 * @param showRowDividers Renders the hairline between rows, inset to
 *   [rowContentPadding].
 * @param rowBackground Background of one row — zebra stripes, selection, a
 *   danger tint; return [Color.Transparent] for the default transparent row.
 * @param onRowClick Makes every row tappable (with the state layer). For
 *   per-cell targets, put clickables inside [RuiTableScope.column] cells.
 * @param sort The active sort, or null when nothing is sorted.
 * @param onSortChange Called when a sortable header is tapped; omitting it
 *   leaves the headers inert.
 */
@Composable
public fun <T> RuiTable(
    count: Int,
    itemAt: (index: Int) -> T,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    key: ((item: T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    rowContentPadding: PaddingValues = PaddingValues(horizontal = RuiTheme.spacing.lg),
    showHeader: Boolean = true,
    showRowDividers: Boolean = true,
    colors: RuiTableColors = RuiTableDefaults.colors(),
    rowBackground: ((item: T, index: Int) -> Color)? = null,
    onRowClick: ((item: T) -> Unit)? = null,
    sort: RuiTableSort? = null,
    onSortChange: ((sort: RuiTableSort) -> Unit)? = null,
    content: RuiTableScope<T>.() -> Unit,
) {
    val columns = RuiTableScope<T>().apply(content).columns
    require(columns.isNotEmpty()) { "RuiTable needs at least one column — declare one with column()." }
    columns.forEachIndexed { index, column ->
        val width = column.width
        if (width is RuiTableColumnWidth.Weighted) {
            require(width.weight > 0f) { "Column $index has a weight of ${width.weight}; weights must be positive." }
        }
    }

    val layoutDirection = LocalLayoutDirection.current

    Column(modifier = modifier) {
        if (showHeader) {
            val headerInsets = ruiTableHeaderInsets(contentPadding, rowContentPadding, layoutDirection)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = headerInsets.start,
                        top = rowContentPadding.calculateTopPadding(),
                        end = headerInsets.end,
                        bottom = rowContentPadding.calculateBottomPadding(),
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
            ) {
                columns.forEach { column ->
                    RuiTableHeaderCell(
                        column = column,
                        cellModifier = column.cellModifier(this),
                        activeSort = sort?.takeIf { column.sortable && it.column == column.sortKey },
                        interactive = column.sortable && onSortChange != null,
                        headerContent = colors.headerContent,
                        onSort = { onSortChange?.invoke(nextTableSort(sort, column.sortKey)) },
                    )
                }
            }
            RuiTableHairline(
                modifier = Modifier.padding(
                    start = headerInsets.start,
                    end = headerInsets.end,
                ),
                color = colors.divider,
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = state,
            contentPadding = contentPadding,
        ) {
            items(
                count = count,
                key = { index -> key?.invoke(itemAt(index)) ?: index },
            ) { index ->
                val item = itemAt(index)
                Column(modifier = Modifier.fillMaxWidth()) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = RuiTheme.dimens.touchTarget)
                            .then(
                                rowBackground?.let { background ->
                                    Modifier.background(background(item, index))
                                } ?: Modifier,
                            )
                            .then(
                                if (onRowClick != null) {
                                    Modifier.clickable(
                                        interactionSource = interactionSource,
                                        indication = rememberRuiIndication(),
                                        onClick = { onRowClick(item) },
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .padding(rowContentPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
                    ) {
                        columns.forEach { column ->
                            Box(
                                modifier = column.cellModifier(this),
                                contentAlignment = CellAlignment(column.alignment),
                            ) {
                                column.cell(item)
                            }
                        }
                    }
                    if (showRowDividers && index < count - 1) {
                        RuiTableHairline(
                            modifier = Modifier.padding(
                                start = rowContentPadding.calculateStartPadding(layoutDirection),
                                end = rowContentPadding.calculateEndPadding(layoutDirection),
                            ),
                            color = colors.divider,
                        )
                    }
                }
            }
        }
    }
}

/**
 * The [List] flavor of [RuiTable] — rows come from [items] instead of a
 * count and an accessor.
 */
@Composable
public fun <T> RuiTable(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    key: ((item: T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    rowContentPadding: PaddingValues = PaddingValues(horizontal = RuiTheme.spacing.lg),
    showHeader: Boolean = true,
    showRowDividers: Boolean = true,
    colors: RuiTableColors = RuiTableDefaults.colors(),
    rowBackground: ((item: T, index: Int) -> Color)? = null,
    onRowClick: ((item: T) -> Unit)? = null,
    sort: RuiTableSort? = null,
    onSortChange: ((sort: RuiTableSort) -> Unit)? = null,
    content: RuiTableScope<T>.() -> Unit,
) {
    RuiTable(
        count = items.size,
        itemAt = { index -> items[index] },
        modifier = modifier,
        state = state,
        key = key,
        contentPadding = contentPadding,
        rowContentPadding = rowContentPadding,
        showHeader = showHeader,
        showRowDividers = showRowDividers,
        colors = colors,
        rowBackground = rowBackground,
        onRowClick = onRowClick,
        sort = sort,
        onSortChange = onSortChange,
        content = content,
    )
}

/** Resolves the column's width policy into the modifier for one of its cells. */
private fun RuiTableColumn<*>.cellModifier(row: RowScope): Modifier = when (val width = width) {
    is RuiTableColumnWidth.Fixed -> Modifier.width(width.value)
    is RuiTableColumnWidth.Weighted -> with(row) { Modifier.weight(width.weight) }
}

@Composable
private fun RuiTableHeaderCell(
    column: RuiTableColumn<*>,
    cellModifier: Modifier,
    activeSort: RuiTableSort?,
    interactive: Boolean,
    headerContent: Color,
    onSort: () -> Unit,
) {
    // The header reads as one more row: the cells carry the same minimum
    // height the data rows do, no special compact padding.
    val cellModifier = cellModifier.heightIn(min = RuiTheme.dimens.touchTarget)
    if (interactive) {
        val interactionSource = remember(column.sortKey) { MutableInteractionSource() }
        val strings = RuiTheme.strings
        Box(
            modifier = cellModifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRuiIndication(),
                    role = Role.Button,
                    onClick = onSort,
                )
                .semantics {
                    if (activeSort != null) {
                        stateDescription = if (activeSort.direction == RuiTableSortDirection.Ascending) {
                            strings.sortedAscending
                        } else {
                            strings.sortedDescending
                        }
                    }
                },
            contentAlignment = CellAlignment(column.alignment),
        ) {
            RuiTableHeaderContent(column, activeSort?.direction, headerContent)
        }
    } else {
        Box(
            modifier = cellModifier,
            contentAlignment = CellAlignment(column.alignment),
        ) {
            RuiTableHeaderContent(column, null, headerContent)
        }
    }
}

@Composable
private fun RuiTableHeaderContent(
    column: RuiTableColumn<*>,
    direction: RuiTableSortDirection?,
    content: Color,
) {
    val header = column.header
    if (header != null) {
        header()
    } else if (column.title != null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RuiText(
                text = column.title,
                style = RuiTheme.typography.labelMd,
                color = content,
                maxLines = 1,
            )
            if (direction != null) {
                RuiTableSortGlyph(direction = direction, tint = content)
            }
        }
    }
}

@Composable
private fun RuiTableSortGlyph(direction: RuiTableSortDirection, tint: Color) {
    // ChevronDown points down at rest; ascending flips it up.
    val rotation by animateFloatAsState(
        targetValue = if (direction == RuiTableSortDirection.Descending) 0f else 180f,
        animationSpec = RuiTheme.motion.normal,
        label = "rui_table_sort_glyph",
    )
    RuiIcon(
        imageVector = RuiIcons.ChevronDown,
        contentDescription = null,
        size = 14.dp,
        tint = tint,
        modifier = Modifier.graphicsLayer { rotationZ = rotation },
    )
}

/** The table's hairline, shared by the header and the row gaps. */
@Composable
private fun RuiTableHairline(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(0.8.dp)
            .background(color),
    )
}

/** Vertically centers a cell's content under its column's horizontal alignment. */
private data class CellAlignment(private val horizontal: Alignment.Horizontal) : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset = IntOffset(
        horizontal.align(size.width, space.width, layoutDirection),
        Alignment.CenterVertically.align(size.height, space.height),
    )
}
