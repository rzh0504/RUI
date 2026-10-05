package rui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import rui.theme.RuiTheme
import kotlin.math.abs

/**
 * A wheel drum, iOS style: the rows scroll and snap so the selected row rests
 * in the center; rows fade and shrink towards the edges and a hairline marks
 * the selection. Every row the drum passes over reports through
 * [onSelectedIndexChange] with a selection tick.
 *
 * The selected index is hoisted — the caller owns it, which is what lets
 * wrappers clamp one wheel's value when another changes (a date picker
 * shortening February). Setting it programmatically scrolls the drum there;
 * the initial value is applied without animation.
 *
 * @param items The values, ordered so the wheel reads top to bottom.
 * @param selectedIndex The centered row; hoisted, clamped into range.
 * @param visibleRows Odd row count shown at once; an even value is rounded up.
 * @param selectionBar Hairlines above and below the selected row.
 * @param itemContent Renders one row's label, centered in the drum.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun <T> RuiWheelPicker(
    items: List<T>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleRows: Int = 5,
    selectionBar: Boolean = true,
    itemContent: @Composable (item: T) -> Unit,
) {
    val rows = if (visibleRows < 1) 1 else if (visibleRows % 2 == 0) visibleRows + 1 else visibleRows
    val rowHeight = RuiTheme.dimens.pickerRowHeight
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Center)
    val halfViewport = with(LocalDensity.current) {
        rowHeight.toPx() * rows / 2f
    }

    val reportedIndex by rememberUpdatedState(onSelectedIndexChange)
    val hoistedIndex by rememberUpdatedState(selectedIndex)
    var positioned by remember { mutableStateOf(false) }

    fun centeredIndex(): Int {
        val layout = listState.layoutInfo
        if (layout.visibleItemsInfo.isEmpty()) return -1
        val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2f
        return layout.visibleItemsInfo
            .minBy { abs(it.offset + it.size / 2f - viewportCenter) }
            .index
    }

    // The drum owns the gesture; the hoisted value owns the rest position.
    // Whenever the drum settles, it is corrected back to the hoisted value —
    // which is a no-op unless the caller clamped it mid-gesture.
    LaunchedEffect(listState, items.size) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (!scrolling && positioned && items.isNotEmpty()) {
                    val target = hoistedIndex.coerceIn(0, items.lastIndex)
                    if (target != centeredIndex()) {
                        scope.launch { listState.animateScrollToItem(target) }
                    }
                }
            }
    }
    LaunchedEffect(selectedIndex, items.size) {
        if (items.isEmpty()) return@LaunchedEffect
        val target = selectedIndex.coerceIn(0, items.lastIndex)
        if (target != centeredIndex()) {
            if (!positioned) {
                listState.scrollToItem(target)
            } else if (!listState.isScrollInProgress) {
                scope.launch { listState.animateScrollToItem(target) }
            }
        }
        positioned = true
    }
    LaunchedEffect(listState, items.size) {
        var first = true
        snapshotFlow { centeredIndex() }
            .distinctUntilChanged()
            .collect { index ->
                if (index >= 0) {
                    if (first) {
                        first = false
                    } else {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        reportedIndex(index)
                    }
                }
            }
    }

    Box(modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight * rows),
            contentPadding = PaddingValues(vertical = rowHeight * (rows / 2)),
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(items) { index, item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .selectable(
                            selected = index == selectedIndex,
                            interactionSource = null as MutableInteractionSource?,
                            indication = null,
                            onClick = { scope.launch { listState.animateScrollToItem(index) } },
                        )
                        .graphicsLayer {
                            val layout = listState.layoutInfo
                            val info = layout.visibleItemsInfo.firstOrNull { it.index == index }
                            if (info != null) {
                                val viewportCenter = layout.viewportCenter()
                                val distance = (
                                    abs(info.offset + info.size / 2f - viewportCenter) /
                                        halfViewport
                                    ).coerceIn(0f, 1f)
                                alpha = 1f - 0.6f * distance
                                val scale = 1f - 0.2f * distance
                                scaleX = scale
                                scaleY = scale
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    itemContent(item)
                }
            }
        }
        if (selectionBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(rowHeight),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.borderWidth)
                        .align(Alignment.TopStart)
                        .background(colors.outlineSubtle),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.borderWidth)
                        .align(Alignment.BottomStart)
                        .background(colors.outlineSubtle),
                )
            }
        }
    }
}

private fun LazyListLayoutInfo.viewportCenter(): Float =
    (viewportStartOffset + viewportEndOffset) / 2f
