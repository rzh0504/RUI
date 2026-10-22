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
import androidx.compose.foundation.lazy.LazyListItemInfo
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import rui.foundation.rememberRuiHaptics
import rui.theme.RuiTheme
import kotlin.math.abs

/**
 * A wheel drum, iOS style: the rows scroll and snap so the selected row rests
 * in the center; rows fade and shrink towards the edges and a hairline marks
 * the selection. The value commits when the drum settles — the reported index
 * is always the row visibly at rest — and every row the drum passes over
 * ticks. The wheel consumes its own vertical gestures: a drum at either end
 * never scrolls the page beneath it.
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
    val rows = wheelRowCount(visibleRows)
    val rowHeight = RuiTheme.dimens.pickerRowHeight
    val colors = RuiTheme.colors
    val dimens = RuiTheme.dimens
    val haptics = rememberRuiHaptics()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Center)
    val halfViewport = with(LocalDensity.current) {
        rowHeight.toPx() * rows / 2f
    }

    val reportedIndex by rememberUpdatedState(onSelectedIndexChange)
    var positioned by remember { mutableStateOf(false) }
    // The index the drum last reported. The parent echoing it back (writing
    // the reported value into its state) must not read as an external change,
    // or correction and reporting feed each other and the drum never rests.
    var lastReportedIndex by remember { mutableStateOf<Int?>(null) }
    // While an external correction animates, reporting is suppressed so the
    // rows the drum passes on the way are not committed as new values.
    var correcting by remember { mutableStateOf(false) }

    // Item offsets include content padding and are measured from the viewport
    // start. Compare against the actual viewport center; subtracting content
    // padding here would move the detected center away from SnapPosition.Center.
    fun centerDistance(info: LazyListItemInfo): Float {
        val layout = listState.layoutInfo
        val desired = layout.viewportCenter() - info.size / 2f
        return abs(info.offset - desired)
    }

    fun centeredIndex(): Int {
        val layout = listState.layoutInfo
        if (layout.visibleItemsInfo.isEmpty()) return -1
        return layout.visibleItemsInfo.minBy { centerDistance(it) }.index
    }

    // External value changes (the initial position, programmatic sets, a
    // wrapper clamping this wheel from another) move the drum; the drum's
    // own echoes don't.
    LaunchedEffect(selectedIndex, items.size) {
        val target = wheelCorrectionTarget(selectedIndex, items.size, lastReportedIndex)
            ?: return@LaunchedEffect
        if (!positioned) {
            listState.scrollToItem(target)
            positioned = true
        } else {
            correcting = true
            try {
                listState.animateScrollToItem(target)
            } finally {
                correcting = false
            }
        }
        lastReportedIndex = target
    }
    // A slow release never flings, so the snap behavior never runs and the
    // drum would rest between rows. Settling is also the moment the value
    // commits: the reported index is the row the drum visibly rests on, so
    // the committed value can never disagree with the drum. Rows passed on
    // the way only tick — reporting per row would race the settle
    // animations and commit values the drum then moves away from.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (!scrolling && positioned && !correcting &&
                    listState.layoutInfo.visibleItemsInfo.isNotEmpty()
                ) {
                    val settled = centeredIndex()
                    if (settled >= 0) {
                        val report = wheelSettledReport(settled, lastReportedIndex)
                        if (report != null) {
                            lastReportedIndex = report
                            reportedIndex(report)
                        }
                        val info = listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.index == settled }
                        if (info != null && wheelNeedsSnapCorrection(centerDistance(info))) {
                            listState.animateScrollToItem(settled)
                        }
                    }
                }
            }
    }
    // Selection tick per row the drum passes over; no value commitment.
    // The first observed row is the resting position, not a pass — no tick.
    LaunchedEffect(listState, items.size) {
        var lastTicked = -1
        snapshotFlow { centeredIndex() }
            .distinctUntilChanged()
            .collect { index ->
                when (wheelTickAction(lastTicked, index, correcting)) {
                    WheelTickAction.Adopt -> lastTicked = index
                    WheelTickAction.Tick -> {
                        lastTicked = index
                        haptics.selection()
                    }
                    WheelTickAction.None -> {}
                }
            }
    }

    Box(modifier.nestedScroll(ConsumeVerticalScroll)) {
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

/** Rows actually rendered: at least one, even counts rounded up so a center row always exists. */
internal fun wheelRowCount(visibleRows: Int): Int =
    if (visibleRows < 1) 1 else if (visibleRows % 2 == 0) visibleRows + 1 else visibleRows

/**
 * The drum target for an external [selectedIndex], or `null` when nothing may
 * move: an empty wheel, or a change that is only the drum's own echo — the
 * caller writing the just-reported value back must not read as an external
 * change, or correcting and reporting feed each other and the drum never
 * rests. Out-of-range indices clamp to the nearest end.
 */
internal fun wheelCorrectionTarget(selectedIndex: Int, itemCount: Int, lastReportedIndex: Int?): Int? {
    if (itemCount == 0) return null
    val target = selectedIndex.coerceIn(0, itemCount - 1)
    return if (target == lastReportedIndex) null else target
}

/**
 * The value a settled drum commits, or `null` when the rest position changes
 * nothing: nothing is visible, or the drum already sits on the reported row.
 */
internal fun wheelSettledReport(settledIndex: Int, lastReportedIndex: Int?): Int? {
    if (settledIndex < 0) return null
    return if (settledIndex == lastReportedIndex) null else settledIndex
}

/**
 * Whether a settled drum needs a correction scroll to rest exactly on the row
 * (a slow release settles between rows without ever flinging).
 */
internal fun wheelNeedsSnapCorrection(centerDistancePx: Float): Boolean = centerDistancePx >= 1f

/** What the tick collector does with a newly centered row. */
internal enum class WheelTickAction { None, Adopt, Tick }

/**
 * The selection tick for the row the drum passes over. The first observed row
 * is the resting position, not a pass ([WheelTickAction.Adopt]); rows passed
 * while an external correction animates do not tick; no value is committed.
 */
internal fun wheelTickAction(lastTicked: Int, centeredIndex: Int, correcting: Boolean): WheelTickAction = when {
    centeredIndex < 0 || correcting -> WheelTickAction.None
    lastTicked == -1 -> WheelTickAction.Adopt
    centeredIndex != lastTicked -> WheelTickAction.Tick
    else -> WheelTickAction.None
}

/**
 * A wheel owns its vertical gesture end to end: whatever the drum cannot
 * consume — scrolling past either end, leftover fling velocity — is eaten
 * here instead of reaching the page the wheel sits on. Consumption happens
 * on the post passes only: the pre passes are parent-first, and eating
 * there would starve the drum itself.
 */
private val ConsumeVerticalScroll = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        Offset(0f, available.y)

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}
