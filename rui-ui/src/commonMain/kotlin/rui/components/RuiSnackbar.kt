package rui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.theme.RuiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.math.abs

/** How a shown snackbar left the screen. */
public sealed interface RuiSnackbarResult {
    /** The user tapped the action label. */
    public data object ActionPerformed : RuiSnackbarResult

    /** The user swiped the snackbar away. */
    public data object Dismissed : RuiSnackbarResult

    /** The snackbar left on its own after its duration. */
    public data object Timeout : RuiSnackbarResult
}

/** How long a snackbar stays on screen. */
public enum class RuiSnackbarDuration {
    /** About four seconds — acknowledgments. */
    Short,

    /** About ten seconds — messages with an action worth reading. */
    Long,
}

/**
 * One shown snackbar: the message, the optional action and the handles the
 * host UI uses to finish it. Obtain instances from [RuiSnackbarHostState.showSnackbar].
 */
public class RuiSnackbarData internal constructor(
    public val message: String,
    public val actionLabel: String?,
    public val duration: RuiSnackbarDuration,
    private val continuation: kotlinx.coroutines.CancellableContinuation<RuiSnackbarResult>,
) {
    /** Finishes the snackbar as if the user tapped the action. */
    public fun performAction(): Unit = finish(RuiSnackbarResult.ActionPerformed)

    /** Finishes the snackbar as if the user swiped it away. */
    public fun dismiss(): Unit = finish(RuiSnackbarResult.Dismissed)

    internal fun timeout(): Unit = finish(RuiSnackbarResult.Timeout)

    private fun finish(result: RuiSnackbarResult) {
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
}

/**
 * State of a [RuiSnackbarHost]: shows snackbars one at a time — concurrent
 * [showSnackbar] calls queue, each caller resumes only when its own snackbar
 * leaves the screen. State is usually hoisted to the app root so any page can
 * show feedback into the same host.
 */
public class RuiSnackbarHostState {
    private val mutex = Mutex()

    /** The snackbar currently on screen, if any. */
    public var currentSnackbarData: RuiSnackbarData? by mutableStateOf(null)
        private set

    /**
     * Shows a snackbar and suspends until it leaves the screen (action,
     * swipe or timeout).
     *
     * @param message The text shown in the snackbar.
     * @param actionLabel Optional short action label ("Undo"); when set, an
     *   action button is rendered.
     * @param duration How long the snackbar stays when not dismissed early.
     */
    public suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: RuiSnackbarDuration = RuiSnackbarDuration.Short,
    ): RuiSnackbarResult = mutex.withLock {
        try {
            suspendCancellableCoroutine { continuation ->
                currentSnackbarData = RuiSnackbarData(message, actionLabel, duration, continuation)
            }
        } finally {
            currentSnackbarData = null
        }
    }
}

/** Creates and remembers a [RuiSnackbarHostState]. */
@Composable
public fun rememberRuiSnackbarHostState(): RuiSnackbarHostState = remember { RuiSnackbarHostState() }

/**
 * Renders the current snackbar of [hostState] at the bottom of its container —
 * place the host through [RuiScaffold]'s `snackbarHost` slot so it sits above
 * the bottom bar. Android's interaction model (queued, timed, swipe-to-
 * dismiss, optional action) with Rui's floating card visual. The leaving
 * snackbar stays composed through the exit slide (its show call has already
 * resumed), so timeouts animate out instead of vanishing.
 */
@Composable
public fun RuiSnackbarHost(
    hostState: RuiSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val current = hostState.currentSnackbarData
    val spacing = RuiTheme.spacing

    var leaving by remember { mutableStateOf<RuiSnackbarData?>(null) }
    if (current != null) {
        leaving = current
    }

    LaunchedEffect(current) {
        if (current != null) {
            delay(
                when (current.duration) {
                    RuiSnackbarDuration.Short -> 4_000L
                    RuiSnackbarDuration.Long -> 10_000L
                },
            )
            current.timeout()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = current != null,
            enter = slideInVertically(initialOffsetY = { it }),
            // Two heights down, so the card fully clears the bottom inset it
            // is docked above — one height can leave it on screen at removal.
            exit = slideOutVertically(targetOffsetY = { it * 2 }),
        ) {
            key(leaving) {
                leaving?.let { data ->
                    RuiSnackbar(data = data)
                }
            }
        }
    }
}

/**
 * The snackbar card: message, optional action button, swipe horizontally to
 * dismiss — the card follows the finger and is thrown out the side it was
 * dragged toward on release. The card slides only and never fades: any alpha
 * below 1 makes HWUI composite the layer offscreen, which crops the elevation
 * shadow to the layer's bounding rectangle — a square-cornered shadow card
 * peeks out behind the rounded corners while swiping. Shown by
 * [RuiSnackbarHost]; don't compose it directly.
 */
@Composable
public fun RuiSnackbar(
    data: RuiSnackbarData,
    modifier: Modifier = Modifier,
) {
    val colors = RuiTheme.colors
    val spacing = RuiTheme.spacing
    val density = LocalDensity.current
    val motion = RuiTheme.motion
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val swipeThresholdPx = with(density) { 120.dp.toPx() }
    var cardWidthPx by remember { mutableStateOf(0) }

    RuiSurface(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width }
            .graphicsLayer {
                translationX = offsetX.value
            }
            .pointerInput(data) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + amount) }
                    },
                    onDragEnd = {
                        scope.launch {
                            val traveled = offsetX.value
                            if (abs(traveled) > swipeThresholdPx) {
                                offsetX.animateTo(
                                    targetValue = (if (traveled > 0f) 1f else -1f) *
                                        (cardWidthPx + swipeThresholdPx),
                                    animationSpec = motion.tracking,
                                )
                                data.dismiss()
                            } else {
                                offsetX.animateTo(0f, motion.settle)
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f, motion.settle) }
                    },
                )
            },
        shape = RuiTheme.shapes.md,
        color = colors.surfaceElevated,
        elevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = spacing.lg,
                    end = if (data.actionLabel != null) spacing.xs else spacing.lg,
                    top = spacing.md,
                    bottom = spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            RuiText(
                text = data.message,
                style = RuiTheme.typography.bodyMd,
                modifier = Modifier.weight(1f),
            )
            if (data.actionLabel != null) {
                RuiButton(
                    onClick = data::performAction,
                    variant = RuiButtonVariant.Plain,
                    size = RuiButtonSize.Sm,
                    colors = RuiButtonDefaults.colors(RuiButtonVariant.Plain)
                        .copy(content = colors.primary),
                ) {
                    RuiText(data.actionLabel)
                }
            }
        }
    }
}
