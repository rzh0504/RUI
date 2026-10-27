package rui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import rui.foundation.RuiHaptics
import rui.foundation.rememberRuiHaptics
import rui.theme.RuiTheme

/**
 * The drag affordances of a [RuiLazyReorderableColumn] row — the same contract
 * as [RuiReorderableItemScope], on the lazy item scope (rows may use
 * `animateItem` for other purposes, but placement animation is the
 * component's job while reordering).
 */
public interface RuiLazyReorderableItemScope : LazyItemScope {

    /** Marks this element as the drag grip: dragging starts immediately. */
    public fun Modifier.dragHandle(enabled: Boolean = true): Modifier

    /** Marks the whole element draggable after a long press. */
    public fun Modifier.draggableItem(enabled: Boolean = true): Modifier
}

private class LazyReorderableItemScopeImpl(
    private val state: LazyReorderState,
    private val itemKey: Any,
    private val itemDragEnabled: Boolean,
    itemScope: LazyItemScope,
) : RuiLazyReorderableItemScope, LazyItemScope by itemScope {

    override fun Modifier.dragHandle(enabled: Boolean): Modifier =
        dragModifier(key = itemKey, longPress = false, enabled = enabled && itemDragEnabled)

    override fun Modifier.draggableItem(enabled: Boolean): Modifier =
        dragModifier(key = itemKey, longPress = true, enabled = enabled && itemDragEnabled)

    private fun Modifier.dragModifier(key: Any, longPress: Boolean, enabled: Boolean): Modifier {
        if (!enabled) return this
        return pointerInput(key, longPress) {
            if (longPress) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { state.startDrag(key) },
                    onDrag = { change, amount ->
                        change.consume()
                        state.drag(amount.y)
                    },
                    onDragEnd = { state.endDrag() },
                    onDragCancel = { state.endDrag() },
                )
            } else {
                detectDragGestures(
                    onDragStart = { state.startDrag(key) },
                    onDrag = { change, amount ->
                        change.consume()
                        state.drag(amount.y)
                    },
                    onDragEnd = { state.endDrag() },
                    onDragCancel = { state.endDrag() },
                )
            }
        }
    }
}

/**
 * Drag-to-reorder over a [LazyColumn] — the virtualized counterpart of
 * [RuiReorderableColumn] for lists of any length (search results, feeds).
 * The interaction contract is the column version's: [onMove] reports moves
 * *live* while the finger is down, neighbors part with a spring, the lifted
 * row scales up with an opaque fill and a shadow, dragging near the viewport
 * edge autoscrolls with the item pinned under the finger, and lift/drop get
 * the medium/light haptic.
 *
 * The mechanics differ from the column version in where positions come from:
 * slot detection reads the engine's own [LazyListState.layoutInfo] by item
 * key every frame, so item heights are whatever the rows measure — no height
 * bookkeeping — and a move only ever triggers against a visible target.
 *
 * Rows should not apply `Modifier.animateItem()` placement animations: the
 * component animates displacement itself while reordering.
 *
 * @param items The current order; reorder it in [onMove] to match.
 * @param onMove Called live with `(fromIndex, toIndex)` as the drag crosses
 *   neighbors.
 * @param key Identity of an item across moves; must be unique per item, and
 *   doubles as the lazy list's item key.
 * @param state The hoisted scroll state of the inner [LazyColumn].
 * @param contentPadding Viewport padding of the inner [LazyColumn].
 * @param canDrag Per-item gate for both triggers.
 * @param itemContent The row. Apply `dragHandle()` or `draggableItem()`
 *   (from the scope) wherever the drag should start.
 */
@Composable
public fun <T : Any> RuiLazyReorderableColumn(
    items: List<T>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    canDrag: (T) -> Boolean = { true },
    itemContent: @Composable RuiLazyReorderableItemScope.(item: T) -> Unit,
) {
    val haptics = rememberRuiHaptics()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val currentOnMove by rememberUpdatedState(onMove)
    val reorderState = remember(haptics, scope, state) {
        LazyReorderState(
            { state.layoutInfo.visibleItemsInfo },
            { from, to -> currentOnMove(from, to) },
            haptics,
            scope,
        )
    }
    val keyOf: (T) -> Any = { key?.invoke(it) ?: it }

    SideEffect { reorderState.syncOrder(items.map(keyOf)) }

    LaunchedEffect(reorderState, density) {
        snapshotFlow { reorderState.isDragging }
            .filter { it }
            .collectLatest {
                val edge = with(density) { AutoscrollEdge.toPx() }
                val speed = with(density) { AutoscrollSpeed.toPx() }
                var previous = withFrameNanos { it }
                while (reorderState.isDragging) {
                    val now = withFrameNanos { it }
                    val dt = ((now - previous) / 1_000_000_000f).coerceIn(0f, 0.1f)
                    previous = now

                    val dragged = reorderState.draggedInfo() ?: continue
                    val center = dragged.offset + reorderState.dragOffset + dragged.size / 2f
                    val top = state.layoutInfo.viewportStartOffset.toFloat()
                    val bottom = state.layoutInfo.viewportEndOffset.toFloat()
                    val overshoot = when {
                        center < top + edge -> center - (top + edge)
                        center > bottom - edge -> center - (bottom - edge)
                        else -> 0f
                    }
                    if (overshoot != 0f) {
                        val fraction = (abs(overshoot) / edge).coerceAtMost(1f)
                        val step = speed * fraction * dt * (if (overshoot < 0f) -1f else 1f)
                        val scrolled = state.dispatchRawDelta(step)
                        if (scrolled != 0f) reorderState.onAutoScrolled(scrolled)
                    }
                }
            }
    }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
    ) {
        itemsIndexed(items, key = { _, item -> keyOf(item) }) { index, item ->
            val itemKey = keyOf(item)
            val itemDragEnabled = canDrag(item)
            val itemScope = remember(itemKey, itemDragEnabled) {
                LazyReorderableItemScopeImpl(reorderState, itemKey, itemDragEnabled, this)
            }
            LazyReorderItem(reorderState, itemKey, index) {
                itemContent(itemScope, item)
            }
        }
    }
}

/** One row of the lazy reorderable column: translates, lifts, springs home. */
@Composable
private fun LazyReorderItem(
    state: LazyReorderState,
    key: Any,
    index: Int,
    content: @Composable () -> Unit,
) {
    val displacement = remember(state.animationGeneration) { Animatable(0f) }
    val liftShape = RuiTheme.shapes.md
    val liftColor = RuiTheme.colors.surface

    // When the item's slot changes under a live move, its home jumps by the
    // recorded shift; re-base the visual offset from the animated value and
    // spring the rest of the way home.
    LaunchedEffect(index, displacement) {
        val shift = state.takeHomeShift(key)
        if (shift != 0f) displacement.snapTo(displacement.value - shift)
        if (shift != 0f || displacement.value != 0f) {
            displacement.animateTo(0f, spring(dampingRatio = 1f, stiffness = 500f))
        }
    }

    val dragging = state.draggedKey == key
    val settling = state.settlingKey == key
    val lift by animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 800f),
        label = "RuiLazyReorderLift",
    )

    Box(
        modifier = Modifier
            .zIndex(if (dragging || settling) 1f else 0f)
            .graphicsLayer {
                translationY = if (dragging || settling) state.dragOffset else displacement.value
                scaleX = 1f + LiftScale * lift
                scaleY = 1f + LiftScale * lift
                if (lift > 0f) {
                    shape = liftShape
                    shadowElevation = lift * LiftShadow.toPx()
                }
            }
            // The row is transparent at rest; lifted, it becomes an opaque
            // card. The shape may be a squircle, so draw its outline as a
            // path rather than assuming rounded corners.
            .drawBehind {
                if (lift > 0f) {
                    when (val outline = liftShape.createOutline(size, layoutDirection, this)) {
                        is Outline.Rounded -> drawRoundRect(
                            color = liftColor,
                            cornerRadius = CornerRadius(outline.roundRect.topLeftCornerRadius.x),
                            alpha = lift,
                        )
                        is Outline.Generic -> drawPath(outline.path, liftColor, alpha = lift)
                        is Outline.Rectangle -> drawRect(liftColor, alpha = lift)
                    }
                }
            },
    ) {
        content()
    }
}

@Stable
internal class LazyReorderState(
    private val visibleItemsInfo: () -> List<LazyListItemInfo>,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    private val haptics: RuiHaptics,
    private val scope: CoroutineScope,
) {
    /** Item keys in the current order, mirrored from the caller's items. */
    var keys: List<Any> by mutableStateOf(emptyList())
        private set

    var animationGeneration by mutableIntStateOf(0)
        private set

    /** Pending home shifts by key, cumulative until consumed. */
    private val homeShifts = mutableMapOf<Any, Float>()

    var draggedKey: Any? by mutableStateOf(null)
        private set

    /** The item springs home after release; it stays lifted until then. */
    var settlingKey: Any? by mutableStateOf(null)
        private set

    var draggedIndex = -1
        private set

    var dragOffset by mutableFloatStateOf(0f)
        private set

    val isDragging: Boolean
        get() = draggedKey != null

    private var settleJob: Job? = null

    fun syncOrder(newKeys: List<Any>) {
        // move() already advances keys, so a matching echo preserves the drag.
        if (newKeys != keys) resetDrag()
        keys = newKeys.toList()
        homeShifts.keys.retainAll(newKeys.toSet())
    }

    private fun resetDrag() {
        settleJob?.cancel()
        settleJob = null
        draggedKey = null
        draggedIndex = -1
        dragOffset = 0f
        settlingKey = null
        homeShifts.clear()
        animationGeneration++
    }

    fun takeHomeShift(key: Any): Float = homeShifts.remove(key) ?: 0f

    fun startDrag(key: Any) {
        val index = keys.indexOf(key)
        if (index == -1) return
        settleJob?.cancel()
        settlingKey = null
        draggedKey = key
        draggedIndex = index
        dragOffset = 0f
        haptics.impactMedium()
    }

    fun drag(delta: Float) {
        if (!isDragging || keys.getOrNull(draggedIndex) != draggedKey) return
        dragOffset += delta
        val visible = currentVisibleItems() ?: return
        val dragged = visible.firstOrNull { it.key == draggedKey } ?: return
        val center = dragged.offset + dragOffset + dragged.size / 2f
        val target = dragTargetIndex(draggedIndex, center) { index ->
            visible.firstOrNull { it.index == index }?.let { it.offset + it.size / 2f }
        }
        if (target != draggedIndex) move(draggedIndex, target, visible)
    }

    fun endDrag() {
        val key = draggedKey ?: return
        draggedKey = null
        haptics.impactLight()
        if (dragOffset != 0f) {
            settlingKey = key
            val residual = dragOffset
            settleJob = scope.launch {
                animate(
                    initialValue = residual,
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 500f),
                ) { value, _ -> dragOffset = value }
                settlingKey = null
                dragOffset = 0f
            }
        }
    }

    /**
     * Keeps the dragged item pinned under a stationary finger while the list
     * scrolls [scrolledPx] beneath it.
     */
    fun onAutoScrolled(scrolledPx: Float) {
        if (isDragging) dragOffset += scrolledPx
    }

    fun draggedInfo(): LazyListItemInfo? =
        currentVisibleItems()?.firstOrNull { it.key == draggedKey }

    private fun currentVisibleItems(): List<LazyListItemInfo>? {
        val visible = visibleItemsInfo()
        // Layout can lag behind syncOrder() or a move; wait for matching slots.
        if (visible.any { keys.getOrNull(it.index) != it.key }) return null
        return visible
    }

    private fun move(from: Int, to: Int, visible: List<LazyListItemInfo>) {
        val dragged = visible.firstOrNull { it.key == draggedKey } ?: return
        for ((slot, shift) in moveShifts(from, to, dragged.size.toFloat())) {
            val displacedKey = keys.getOrNull(slot) ?: continue
            // Cumulative: two moves can land before the displaced item's
            // effect consumes its pending shift.
            homeShifts[displacedKey] = (homeShifts[displacedKey] ?: 0f) + shift
        }
        // The dragged home travels by the crossed neighbors' sizes — all of
        // them visible, since a move only ever targets a visible slot.
        val homeDelta = if (to > from) {
            visible.filter { it.index in from + 1..to }.sumOf { it.size }.toFloat()
        } else {
            -visible.filter { it.index in to until from }.sumOf { it.size }.toFloat()
        }
        val moved = keys[from]
        keys = keys.toMutableList().apply {
            removeAt(from)
            add(to, moved)
        }
        // Re-base so the item stays under the finger after its home jumps.
        dragOffset -= homeDelta
        draggedIndex = to
        onMove(from, to)
    }
}
