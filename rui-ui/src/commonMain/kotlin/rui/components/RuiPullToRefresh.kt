package rui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import rui.theme.RuiTheme
import kotlinx.coroutines.launch

public object RuiPullToRefreshDefaults {

    /** Distance the pull must reach for release to trigger a refresh. */
    public val TriggerDistance: Dp = 80.dp

    /**
     * Fraction of the overscroll delta that turns into pull distance — the
     * resistance past the content's top edge.
     */
    internal const val DampFactor = 0.5f

    /** Milliseconds the indicator stays shown when the refresh is short. */
    internal const val MinShownMillis = 600L
}

/**
 * A container that turns overscroll at its content's top edge into a
 * pull-to-refresh gesture — Android's refresh interaction model on the
 * system's progress language: pulling grows a [RuiCircularProgress] arc,
 * releasing past the trigger distance snaps the indicator in and calls
 * [onRefresh], and the hoisted [isRefreshing] drives the rest (raising it
 * while idle shows the indicator programmatically; lowering it collapses).
 *
 * The nested-scroll coordination and the threshold haptic are adapted from
 * miuix's PullToRefresh, simplified: no damped-drag physics, no app bar
 * coordination, no text labels. The content should be a scrollable that
 * reaches the top edge (without its own bounce overscroll).
 *
 * @param isRefreshing Whether a refresh is in progress; hoisted, and the
 *   source of truth in both directions.
 * @param onRefresh Called when the user releases past the trigger distance,
 *   or when [isRefreshing] is raised while the indicator is idle.
 */
@Composable
public fun RuiPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val triggerPx = with(density) { RuiPullToRefreshDefaults.TriggerDistance.toPx() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    val isRefreshingNow by rememberUpdatedState(isRefreshing)

    var dragOffset by remember { mutableFloatStateOf(0f) }
    var showing by remember { mutableStateOf(false) }
    var thresholdArmed by remember { mutableStateOf(true) }
    val offsetAnim = remember { Animatable(0f) }

    // Single writer to dragOffset: gestures snap the animatable; programmatic
    // moves animate it and mirror each frame back.
    suspend fun settleTo(target: Float) {
        offsetAnim.snapTo(dragOffset)
        offsetAnim.animateTo(target, spring(dampingRatio = 1f, stiffness = 350f)) {
            dragOffset = value
        }
        dragOffset = target
    }

    // Two-way sync with the hoisted isRefreshing.
    LaunchedEffect(isRefreshing) {
        when {
            isRefreshing && !showing -> {
                showing = true
                settleTo(triggerPx)
            }

            !isRefreshing && showing -> {
                settleTo(0f)
                showing = false
                thresholdArmed = true
            }
        }
    }

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource,
            ): androidx.compose.ui.geometry.Offset {
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }
                // While refreshing the indicator locks the scroll.
                if (showing) {
                    return available
                }
                // Mid-pull, upward scroll retracts the indicator before the
                // content scrolls.
                if (available.y < 0f && dragOffset > 0f) {
                    val consumed = available.y.coerceAtLeast(-dragOffset)
                    dragOffset += consumed
                    scope.launch { offsetAnim.snapTo(dragOffset) }
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource,
            ): androidx.compose.ui.geometry.Offset {
                if (source != NestedScrollSource.UserInput || showing) {
                    return Offset.Zero
                }
                if (available.y > 0f) {
                    dragOffset += available.y * RuiPullToRefreshDefaults.DampFactor
                    if (dragOffset >= triggerPx && thresholdArmed) {
                        thresholdArmed = false
                        haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    }
                    if (dragOffset < triggerPx) {
                        thresholdArmed = true
                    }
                    scope.launch { offsetAnim.snapTo(dragOffset) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (dragOffset > 0f && !showing) {
                    if (dragOffset >= triggerPx) {
                        showing = true
                        settleTo(triggerPx)
                        currentOnRefresh()
                        // A caller that never raises isRefreshing collapses
                        // immediately instead of locking the indicator.
                        if (!isRefreshingNow) {
                            settleTo(0f)
                            showing = false
                            thresholdArmed = true
                        }
                    } else {
                        settleTo(0f)
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = modifier.nestedScroll(connection)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg)
                    .height(with(density) { dragOffset.toDp() }),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (dragOffset > 0f) {
                    val progress = (dragOffset / triggerPx).coerceIn(0f, 1f)
                    RuiCircularProgress(
                        progress = if (showing) null else progress,
                        modifier = Modifier
                            .padding(vertical = RuiTheme.spacing.sm)
                            .graphicsLayer { alpha = progress },
                    )
                }
            }
            content()
        }
    }
}
