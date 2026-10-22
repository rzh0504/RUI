package rui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.foundation.utils.overScrollVertical
import rui.foundation.utils.scrollEndHaptic

/**
 * The lazy flavor of Rui's list DSL: everything foundation's [LazyListScope]
 * offers (items, keys, sticky headers) plus Rui's own [section] convenience.
 */
public interface RuiLazyListScope : LazyListScope {

    /**
     * One bounded grouped section — an optional [header] above the
     * [RuiListSection] card and an optional [footer] below it, emitted as a
     * single item. The rows inside the card are *not* virtualized: sections
     * are for bounded content (settings pages), long content belongs in flat
     * [items]. The header is the item's default key when non-null.
     */
    public fun section(
        header: String? = null,
        footer: String? = null,
        content: @Composable ColumnScope.() -> Unit,
    )
}

private class RuiLazyListScopeImpl(
    private val list: LazyListScope,
) : RuiLazyListScope, LazyListScope by list {

    override fun section(
        header: String?,
        footer: String?,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        item(key = header) {
            RuiListSection(header = header, footer = footer, content = content)
        }
    }
}

/**
 * Rui's virtualized list: foundation's [LazyColumn] wearing the library's
 * platform feel — bounce overscroll at both ends and a boundary haptic when a
 * fling hits them, the same feel every non-lazy Rui list carries — with
 * [contentPadding] following the scaffold convention (padding lives in the
 * viewport, so content scrolls under the glass bars while its ends rest above
 * them).
 *
 * The content DSL is [RuiLazyListScope]; every foundation capability passes
 * through unchanged — `items`, keys, `contentType`, and `stickyHeader` for
 * pinned section headers (give the header an opaque background; content
 * scrolls beneath it).
 *
 * Long content is flat rows via `items`. Grouped settings pages compose
 * [RuiLazyListScope.section] — bounded cards, one item each, deliberately not
 * virtualized inside the card.
 *
 * @param state The hoisted scroll state, as on [LazyColumn].
 * @param contentPadding Viewport padding; merge the scaffold's padding into
 *   it rather than padding the list from outside.
 * @param scrollBar Overlay a [RuiScrollBar] on the list's trailing edge,
 *   sharing this list's scroll state. The thumb's track skips the list's
 *   [contentPadding] so it never rides under the scaffold's bars; on touch it
 *   is an indicator only, and a mouse can drag it.
 */
@Composable
public fun RuiLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    userScrollEnabled: Boolean = true,
    scrollBar: Boolean = false,
    content: RuiLazyListScope.() -> Unit,
) {
    if (!scrollBar) {
        LazyColumn(
            modifier = modifier
                .overScrollVertical()
                .scrollEndHaptic(),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            userScrollEnabled = userScrollEnabled,
        ) {
            RuiLazyListScopeImpl(this).content()
        }
        return
    }

    Box(modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .scrollEndHaptic(),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            userScrollEnabled = userScrollEnabled,
        ) {
            RuiLazyListScopeImpl(this).content()
        }
        RuiScrollBar(
            adapter = rememberRuiScrollBarAdapter(state),
            modifier = Modifier.align(Alignment.CenterEnd),
            reverseLayout = reverseLayout,
            trackPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
        )
    }
}
