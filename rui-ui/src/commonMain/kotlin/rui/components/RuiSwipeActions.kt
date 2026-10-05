package rui.components

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.foundation.rememberRuiIndication
import rui.theme.RuiMotion
import rui.theme.RuiTheme
import kotlin.math.roundToInt

/** Where a swiped row currently rests. */
public enum class RuiSwipeValue {
    /** The row covers its actions. */
    Closed,

    /** The actions at the row's start edge are revealed. */
    StartOpen,

    /** The actions at the row's end edge are revealed. */
    EndOpen,
}

/**
 * Coordinates a set of swipe rows so at most one is open, iOS style: opening
 * a row closes the previously opened one. Remember one per list.
 */
@Stable
public class RuiSwipeGroup internal constructor() {
    private var openState: RuiSwipeActionsState? = null

    internal fun claim(state: RuiSwipeActionsState) {
        val previous = openState
        openState = state
        if (previous != null && previous != state) {
            previous.close()
        }
    }

    internal fun release(state: RuiSwipeActionsState) {
        if (openState == state) {
            openState = null
        }
    }
}

/** Remember a [RuiSwipeGroup] for a list of swipe rows. */
@Composable
public fun rememberRuiSwipeGroup(): RuiSwipeGroup =
    remember { RuiSwipeGroup() }

/**
 * Drag and settle state of a swipe row. The row opens when the drag passes
 * half the distance to its actions and settles back otherwise.
 *
 * Pass a [group] to make the row close when another row of the same group opens.
 */
@Stable
public class RuiSwipeActionsState internal constructor(
    density: Density,
    motion: RuiMotion,
    decayAnimationSpec: DecayAnimationSpec<Float>,
    private val scope: CoroutineScope,
) {
    internal val draggableState = AnchoredDraggableState(
        initialValue = RuiSwipeValue.Closed,
        positionalThreshold = { totalDistance -> totalDistance / 2f },
        velocityThreshold = { with(density) { VelocityThreshold.toPx() } },
        snapAnimationSpec = motion.settle,
        decayAnimationSpec = decayAnimationSpec,
    )

    /** The value the row is resting on (the last settled value while dragging). */
    public val currentValue: RuiSwipeValue
        get() = draggableState.settledValue

    /** Open the row towards [target]; a no-op for [RuiSwipeValue.Closed] — use [close]. */
    public fun open(target: RuiSwipeValue) {
        if (target == RuiSwipeValue.Closed) return
        scope.launch { draggableState.animateTo(target) }
    }

    /** Close the row. */
    public fun close() {
        scope.launch { draggableState.animateTo(RuiSwipeValue.Closed) }
    }

    internal fun updateAnchors(startWidthPx: Float, endWidthPx: Float) {
        val anchors = DraggableAnchors<RuiSwipeValue> {
            RuiSwipeValue.Closed at 0f
            if (startWidthPx > 0f) RuiSwipeValue.StartOpen at startWidthPx
            if (endWidthPx > 0f) RuiSwipeValue.EndOpen at -endWidthPx
        }
        draggableState.updateAnchors(anchors)
    }

    private companion object {
        val VelocityThreshold = 125.dp
    }
}

/**
 * Remember the state of a swipe row. Rows sharing a [group] are mutually
 * exclusive: settling one open closes the previously opened one.
 */
@Composable
public fun rememberRuiSwipeActionsState(
    group: RuiSwipeGroup? = null,
): RuiSwipeActionsState {
    val density = LocalDensity.current
    val motion = RuiTheme.motion
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scope = rememberCoroutineScope()
    val state = remember(density, motion, decayAnimationSpec, scope) {
        RuiSwipeActionsState(density, motion, decayAnimationSpec, scope)
    }
    if (group != null) {
        LaunchedEffect(state, group) {
            snapshotFlow { state.currentValue }.collect { value ->
                if (value == RuiSwipeValue.Closed) {
                    group.release(state)
                } else {
                    group.claim(state)
                }
            }
        }
    }
    return state
}

private enum class SwipeSlot { Content, Start, End }

/**
 * A list row whose horizontal drag reveals full-height action buttons behind
 * it, iOS style. Dragging the row towards its end exposes the buttons of
 * [endActions] at the end edge; dragging towards its start exposes
 * [startActions]. A drag settling past half the action width opens the row,
 * a fling opens it regardless. Tapping a revealed action closes the row
 * first, then reports through [RuiActionOption.onSelect].
 *
 * The row's content is drawn on top of the actions and keeps its own
 * gestures — [content] is typically a [RuiListItem]. The actions use the
 * [RuiActionOption] data type of [RuiActionPanel]: `danger` options render
 * with the danger fill, others with the muted surface.
 *
 * Rows are usually placed inside a [RuiListGroup], which clips the revealed
 * buttons to the card's shape.
 */
@Composable
public fun RuiSwipeActions(
    state: RuiSwipeActionsState,
    endActions: List<RuiActionOption>,
    modifier: Modifier = Modifier,
    startActions: List<RuiActionOption> = emptyList(),
    enabled: Boolean = true,
    actionWidth: Dp = SwipeActionWidth,
    content: @Composable () -> Unit,
) {
    var startWidthPx by remember { mutableFloatStateOf(0f) }
    var endWidthPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(startWidthPx, endWidthPx) {
        state.updateAnchors(startWidthPx, endWidthPx)
    }

    SubcomposeLayout(
        modifier = modifier.anchoredDraggable(
            state = state.draggableState,
            orientation = Orientation.Horizontal,
            enabled = enabled,
        ),
    ) { constraints ->
        val contentPlaceables = subcompose(SwipeSlot.Content) { content() }
            .map { it.measure(constraints) }
        val contentWidth = contentPlaceables.maxOfOrNull { it.width } ?: 0
        val contentHeight = contentPlaceables.maxOfOrNull { it.height } ?: 0

        val heightLock = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = contentHeight,
            maxHeight = contentHeight,
        )
        val startPlaceables = if (startActions.isEmpty()) {
            emptyList()
        } else {
            subcompose(SwipeSlot.Start) {
                SwipeActionButtons(state, startActions, actionWidth)
            }.map { it.measure(heightLock) }
        }
        val endPlaceables = if (endActions.isEmpty()) {
            emptyList()
        } else {
            subcompose(SwipeSlot.End) {
                SwipeActionButtons(state, endActions, actionWidth)
            }.map { it.measure(heightLock) }
        }

        startWidthPx = (startPlaceables.maxOfOrNull { it.width } ?: 0).toFloat()
        endWidthPx = (endPlaceables.maxOfOrNull { it.width } ?: 0).toFloat()

        val offset = state.draggableState.offset
        val offsetX = if (offset.isNaN()) 0 else offset.roundToInt()

        layout(contentWidth, contentHeight) {
            startPlaceables.forEach { it.placeRelative(0, 0) }
            endPlaceables.forEach { it.placeRelative(contentWidth - it.width, 0) }
            contentPlaceables.forEach { it.placeRelative(offsetX, 0) }
        }
    }
}

/** The row of revealed buttons, sized to the row height by the caller's measure pass. */
@Composable
private fun SwipeActionButtons(
    state: RuiSwipeActionsState,
    actions: List<RuiActionOption>,
    actionWidth: Dp,
) {
    Row {
        actions.forEach { option ->
            SwipeActionButton(state, option, actionWidth)
        }
    }
}

@Composable
private fun SwipeActionButton(
    state: RuiSwipeActionsState,
    option: RuiActionOption,
    width: Dp,
) {
    val colors = RuiTheme.colors
    val container = if (option.danger) colors.danger else colors.surfaceMuted
    val contentColor = when {
        !option.enabled -> colors.contentDisabled
        option.danger -> Color.White
        else -> colors.content
    }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRuiIndication(),
                enabled = option.enabled,
                onClickLabel = option.label,
                role = Role.Button,
            ) {
                state.close()
                option.onSelect()
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xs),
        ) {
            option.icon?.let { icon: ImageVector ->
                RuiIcon(imageVector = icon, contentDescription = null, tint = contentColor)
            }
            RuiText(
                text = option.label,
                style = RuiTheme.typography.labelMd,
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val SwipeActionWidth = 72.dp
