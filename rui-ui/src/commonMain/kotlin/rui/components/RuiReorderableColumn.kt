package rui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
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
 * Slot the vertical [center] falls into among the slots' [heights] (px): the
 * first slot whose midpoint the center passes.
 */
internal fun slotIndexOf(center: Float, heights: FloatArray): Int {
    var acc = 0f
    for (i in heights.indices) {
        val mid = acc + heights[i] / 2f
        if (center < mid) return i
        acc += heights[i]
    }
    return heights.lastIndex
}

/** Top edge (px) of the slot at [index], given each slot's [heights]. */
internal fun homeTopOf(index: Int, heights: FloatArray): Float {
    var acc = 0f
    for (i in 0 until index.coerceAtMost(heights.size)) acc += heights[i]
    return acc
}

/**
 * Home displacement of the other slots when an item moves from slot [from]
 * to [to]: everything the item travels over is pushed aside by its height,
 * against the direction of the move. Returns pre-move slot index → shift.
 */
internal fun moveShifts(from: Int, to: Int, draggedHeight: Float): Map<Int, Float> = buildMap {
    if (to > from) {
        for (i in from + 1..to) put(i, -draggedHeight)
    } else {
        for (i in to until from) put(i, draggedHeight)
    }
}

/** How far a lifted item scales up while dragging. */
private const val LiftScale = 0.03f

/** Shadow elevation of a lifted item. */
private val LiftShadow = 14.dp

/** Width of the edge band that drives autoscroll while dragging. */
private val AutoscrollEdge = 56.dp

/** Full autoscroll speed, reached at the viewport edge (px/s). */
private val AutoscrollSpeed = 640.dp

/**
 * Drag-to-reorder over a plain Column — the reorderable counterpart of the
 * library's non-lazy list primitives. Dragging an item reports moves through
 * [onMove] *live* (while the finger is down, every time the item's center
 * crosses a neighbor), so the caller's list and the view stay in lockstep;
 * the items the dragged one travels over part with a spring and settle back.
 *
 * Two drag triggers exist, both created from the item scope:
 * `Modifier.dragHandle()` starts immediately from a dedicated grip, and
 * `Modifier.draggableItem()` starts after a long press on its element. Both
 * give lift/crossing/drop haptics.
 *
 * The container is not lazy — it lays out every item, like the rest of Rui's
 * list components — so it suits the moderate list sizes a reorderable list
 * implies. When it lives inside a `verticalScroll` container, pass that
 * container's [ScrollState] as [autoscrollState]: dragging near the viewport
 * edge scrolls it, with the item pinned under the finger.
 *
 * Items are matched by [key] across moves — when omitted, the item itself is
 * the key, which requires distinct item instances (supply a key when items
 * can be equal). Moves already reported when a drag is cancelled stand; the
 * item springs back to its slot.
 *
 * @param items The current order; reorder it in [onMove] to match.
 * @param onMove Called live with `(fromIndex, toIndex)` as the drag crosses
 *   neighbors.
 * @param key Identity of an item across moves; must be unique per item.
 * @param autoscrollState The enclosing container's scroll state, enabling
 *   edge autoscroll.
 * @param canDrag Per-item gate for both triggers.
 * @param itemContent The row. Apply `dragHandle()` or `draggableItem()`
 *   (from the scope) wherever the drag should start.
 */
@Composable
public fun <T : Any> RuiReorderableColumn(
    items: List<T>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    autoscrollState: ScrollState? = null,
    canDrag: (T) -> Boolean = { true },
    itemContent: @Composable RuiReorderableItemScope.(item: T) -> Unit,
) {
    val haptics = rememberRuiHaptics()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val currentOnMove by rememberUpdatedState(onMove)
    val state = remember(haptics, scope) {
        ReorderState({ from, to -> currentOnMove(from, to) }, haptics, scope)
    }
    val keyOf: (T) -> Any = { key?.invoke(it) ?: it }

    SideEffect { state.syncOrder(items.map(keyOf)) }

    LaunchedEffect(state, autoscrollState) {
        val scroller = autoscrollState ?: return@LaunchedEffect
        snapshotFlow { state.isDragging }
            .filter { it }
            .collectLatest {
                val edge = with(density) { AutoscrollEdge.toPx() }
                val speed = with(density) { AutoscrollSpeed.toPx() }
                var previous = withFrameNanos { it }
                while (state.isDragging) {
                    val now = withFrameNanos { it }
                    val dt = ((now - previous) / 1_000_000_000f).coerceIn(0f, 0.1f)
                    previous = now

                    val center = state.draggedCenter() ?: continue
                    val top = maxOf(state.containerTopInWindow, 0f)
                    val bottom = minOf(state.containerBottomInWindow, state.windowHeight)
                    val overshoot = when {
                        center < top + edge -> center - (top + edge)
                        center > bottom - edge -> center - (bottom - edge)
                        else -> 0f
                    }
                    if (overshoot != 0f) {
                        val fraction = (abs(overshoot) / edge).coerceAtMost(1f)
                        val step = speed * fraction * dt * (if (overshoot < 0f) -1f else 1f)
                        val scrolled = scroller.scrollBy(step)
                        if (scrolled != 0f) state.onAutoScrolled(scrolled)
                    }
                }
            }
    }

    Column(
        modifier = modifier.onGloballyPositioned { coords ->
            state.containerTopInWindow = coords.positionInWindow().y
            state.containerBottomInWindow = coords.positionInWindow().y + coords.size.height
            state.windowHeight = coords.findRootCoordinates().size.height.toFloat()
        },
    ) {
        items.forEachIndexed { index, item ->
            val itemKey = keyOf(item)
            key(itemKey) {
                val itemDragEnabled = canDrag(item)
                val itemScope = remember(itemKey, itemDragEnabled) {
                    ReorderableItemScopeImpl(state, itemKey, itemDragEnabled)
                }
                ReorderItem(state, itemKey, index) {
                    itemContent(itemScope, item)
                }
            }
        }
    }
}

/**
 * The drag affordances of a [RuiReorderableColumn] row. The scope is per
 * item; the modifiers it creates carry the item's identity, so they keep
 * working when applied outside the scope (a trailing slot, say) — capture
 * and pass them along.
 */
public interface RuiReorderableItemScope {

    /** Marks this element as the drag grip: dragging starts immediately. */
    public fun Modifier.dragHandle(enabled: Boolean = true): Modifier

    /** Marks the whole element draggable after a long press. */
    public fun Modifier.draggableItem(enabled: Boolean = true): Modifier
}

private class ReorderableItemScopeImpl(
    private val state: ReorderState,
    private val itemKey: Any,
    private val itemDragEnabled: Boolean,
) : RuiReorderableItemScope {

    override fun Modifier.dragHandle(enabled: Boolean): Modifier =
        dragModifier(key = itemKey, longPress = false, enabled = enabled && itemDragEnabled)

    override fun Modifier.draggableItem(enabled: Boolean): Modifier =
        dragModifier(key = itemKey, longPress = true, enabled = enabled && itemDragEnabled)

    private fun Modifier.dragModifier(key: Any, longPress: Boolean, enabled: Boolean): Modifier {
        if (!enabled) return this
        return pointerInput(key, longPress) {
            if (longPress) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { state.startDrag(key, fromLongPress = true) },
                    onDrag = { change, amount ->
                        change.consume()
                        state.drag(amount.y)
                    },
                    onDragEnd = { state.endDrag() },
                    onDragCancel = { state.endDrag() },
                )
            } else {
                detectDragGestures(
                    onDragStart = { state.startDrag(key, fromLongPress = false) },
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

/** One row of the reorderable column: measures, translates, lifts. */
@Composable
private fun ReorderItem(
    state: ReorderState,
    key: Any,
    index: Int,
    content: @Composable () -> Unit,
) {
    val displacement = remember { Animatable(0f) }
    val liftShape = RuiTheme.shapes.md
    val liftColor = RuiTheme.colors.surface

    // When the item's slot changes under a live move, its home jumps by the
    // recorded shift; re-base the visual offset from the animated value and
    // spring the rest of the way home.
    LaunchedEffect(index) {
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
        label = "RuiReorderLift",
    )

    Box(
        modifier = Modifier
            .onSizeChanged { state.reportHeight(key, it.height) }
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
            // card — without a fill, the shadow above bleeds through and
            // reads as a grey slab. The shape may be a squircle, so draw its
            // outline as a path rather than assuming rounded corners.
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
            }
            .onGloballyPositioned { coords ->
                state.reportCenter(key, coords.positionInWindow().y + coords.size.height / 2f)
            },
    ) {
        content()
    }
}

@Stable
private class ReorderState(
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    private val haptics: RuiHaptics,
    private val scope: CoroutineScope,
) {
    /** Item keys in the current order, mirrored from the caller's items. */
    var keys: List<Any> by mutableStateOf(emptyList())

    /** Measured heights by key (px); keyed, so they survive moves. */
    private val heightsByItem = mutableMapOf<Any, Int>()

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

    /** Latest window-space vertical center per item, reported on layout. */
    private val centersByItem = mutableMapOf<Any, Float>()

    var containerTopInWindow by mutableFloatStateOf(0f)
    var containerBottomInWindow by mutableFloatStateOf(0f)
    var windowHeight by mutableFloatStateOf(0f)

    val isDragging: Boolean
        get() = draggedKey != null

    private var settleJob: Job? = null

    fun syncOrder(newKeys: List<Any>) {
        // During a drag the mirror is advanced by move(); only sync at rest.
        if (!isDragging) {
            keys = newKeys
            centersByItem.keys.retainAll(newKeys.toSet())
        }
    }

    fun reportHeight(key: Any, height: Int) {
        heightsByItem[key] = height
    }

    fun reportCenter(key: Any, centerInWindow: Float) {
        centersByItem[key] = centerInWindow
    }

    /** Window-space vertical center of the dragged item, or null before its
     *  first layout — always a real measurement, never a stale one. */
    fun draggedCenter(): Float? = draggedKey?.let { centersByItem[it] }

    fun takeHomeShift(key: Any): Float = homeShifts.remove(key) ?: 0f

    fun startDrag(key: Any, fromLongPress: Boolean) {
        settleJob?.cancel()
        settlingKey = null
        draggedKey = key
        draggedIndex = keys.indexOf(key)
        dragOffset = 0f
        haptics.impactMedium()
    }

    fun drag(delta: Float) {
        if (!isDragging || draggedIndex !in keys.indices) return
        dragOffset += delta
        val heights = currentHeights()
        if (heights.isEmpty() || heights.any { it == 0f }) return
        val draggedHeight = heights[draggedIndex]
        val center = homeTopOf(draggedIndex, heights) + dragOffset + draggedHeight / 2f
        val target = slotIndexOf(center, heights)
        if (target != draggedIndex) move(draggedIndex, target)
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
     * Keeps the dragged item pinned under a stationary finger while the
     * container scrolls [scrolledPx] beneath it.
     */
    fun onAutoScrolled(scrolledPx: Float) {
        if (isDragging) dragOffset += scrolledPx
    }

    private fun currentHeights(): FloatArray =
        FloatArray(keys.size) { (heightsByItem[keys[it]] ?: 0).toFloat() }

    private fun move(from: Int, to: Int) {
        val heights = currentHeights()
        val draggedHeight = heights[from]
        val homeBefore = homeTopOf(from, heights)
        for ((slot, shift) in moveShifts(from, to, draggedHeight)) {
            // Cumulative: two moves can land before the displaced item's
            // effect consumes its pending shift.
            homeShifts[keys[slot]] = (homeShifts[keys[slot]] ?: 0f) + shift
        }
        val moved = keys[from]
        keys = keys.toMutableList().apply {
            removeAt(from)
            add(to, moved)
        }
        val homeAfter = homeTopOf(to, currentHeights())
        dragOffset += homeBefore - homeAfter
        draggedIndex = to
        onMove(from, to)
    }
}
