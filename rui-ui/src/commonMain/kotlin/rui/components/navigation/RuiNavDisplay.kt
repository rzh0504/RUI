package rui.components.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import rui.foundation.LocalRuiTransitionActive
import rui.theme.RuiTheme

/**
 * Input filters on an entry's host must not share a pointerInput key with any
 * other filter on the same node chain: an equal key updates the node in place
 * instead of restarting its handler coroutine.
 */
private const val OpaqueInputKey = "rui.components.navigation.opaqueInput"

/**
 * Marker interface for routes rendered by [RuiNavDisplay]. Modeled after the
 * miuix-nav module's NavKey (https://github.com/composable-miuix-ui/miuix,
 * Apache License 2.0); the stack/display model below is a lightweight
 * reduction of that design — a single animated float drives every visible
 * entry, so multi-step pushes and pops move as one continuous motion.
 */
public interface RuiNavKey

/**
 * An observable back stack for [RuiNavDisplay]. The stack is kept in memory
 * only (no process-death restoration yet — see the roadmap).
 */
@Composable
public fun <T : RuiNavKey> rememberRuiNavBackStack(initial: T): androidx.compose.runtime.snapshots.SnapshotStateList<T> =
    remember { mutableStateListOf(initial) }

/**
 * Convenience operations over a [rememberRuiNavBackStack]. The stack always
 * keeps at least its root entry; [pop] at the root is a no-op.
 */
@Stable
public class RuiNavController<T : RuiNavKey>(public val backStack: androidx.compose.runtime.snapshots.SnapshotStateList<T>) {

    public val current: T?
        get() = backStack.lastOrNull()

    public fun push(key: T) {
        backStack.add(key)
    }

    public fun pop(): Boolean =
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }

    /** Replaces the top entry without a transition. */
    public fun replace(key: T) {
        require(backStack.isNotEmpty()) { "replace() on an empty back stack" }
        backStack[backStack.lastIndex] = key
    }

    public fun popUntil(predicate: (T) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}

/**
 * Renders the [backStack] as a continuous stack: the entry at the top of the
 * stack is fully visible, covered entries slide left with a slight parallax
 * and dim, and pushing slides the new entry in from the right. A single
 * animated float drives every entry, so rapid pushes and pops compose into
 * one motion.
 *
 * Platform back presses (while the stack has more than the root entry) are
 * routed to [onBack]; call `backStack.removeAt(backStack.lastIndex)` there —
 * or hand the stack to [RuiNavController] and use [RuiNavController.pop].
 * Entries removed by a pop stay composed until the pop animation settles.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
public fun <T : RuiNavKey> RuiNavDisplay(
    backStack: List<T>,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    require(backStack.isNotEmpty()) { "RuiNavDisplay requires a non-empty back stack." }

    val renderStack = remember { mutableStateListOf(backStack.first()) }
    val animatedTop = remember { Animatable(0f) }
    var transitionRunning by remember { mutableStateOf(false) }
    val transitionSpec = RuiTheme.motion.navigationDriver

    LaunchedEffect(backStack.size, backStack.lastOrNull()) {
        // Pushes join the render stack immediately so the new entry slides in.
        while (renderStack.size < backStack.size) {
            renderStack.add(backStack[renderStack.size])
        }
        // Replaces swap the top entry in place (no transition in v1). The
        // size equality is what keeps this from also firing on a pop: a pop
        // deliberately leaves the exiting entry composed (it has to slide
        // out), so the two tops legitimately differ until the removal loop
        // below runs — replacing it there would slide out a copy of the
        // destination instead of the page being left.
        if (renderStack.size == backStack.size && renderStack.last() != backStack.last()) {
            renderStack[renderStack.lastIndex] = backStack.last()
        }

        // Already at target (initial launch, or an in-place replace): no
        // motion to drive — running the tween would hold the transition flag
        // up for its full duration over a zero distance.
        if (animatedTop.value == (backStack.size - 1).toFloat()) return@LaunchedEffect

        transitionRunning = true
        animatedTop.animateTo(
            (backStack.size - 1).toFloat(),
            transitionSpec,
        )
        // Let the settled frame render before structural changes land.
        withFrameNanos { }
        transitionRunning = false

        // Pops: only once the motion settles do the removed entries leave the
        // render stack, which is what lets them animate out to the right.
        while (renderStack.size > backStack.size) {
            renderStack.removeAt(renderStack.lastIndex)
        }
    }

    if (onBack != null && backStack.size > 1) {
        BackHandler(onBack = onBack)
    }

    CompositionLocalProvider(LocalRuiTransitionActive provides transitionRunning) {
        var containerWidthPx by remember { mutableIntStateOf(0) }

        Box(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { containerWidthPx = it.width },
        ) {
            renderStack.forEachIndexed { index, key ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Pages are input-opaque within their bounds: a
                        // hit-testable but non-consuming filter keeps a touch
                        // on a blank region of this entry (a page shorter
                        // than the screen) from falling through to a covered
                        // entry below. Mechanism from miuix-nav's entry host.
                        .pointerInput(OpaqueInputKey) {
                            awaitPointerEventScope {
                                while (true) awaitPointerEvent()
                            }
                        }
                        .graphicsLayer {
                            val depth = animatedTop.value - index
                            val width = containerWidthPx.toFloat()
                            when {
                                depth <= -1f -> {
                                    translationX = width
                                    alpha = 0f
                                }
                                depth >= 1f -> {
                                    translationX = -width * 0.25f * depth
                                    alpha = 0f
                                }
                                depth < 0f -> {
                                    // Incoming: slides in from the right.
                                    translationX = -depth * width
                                    alpha = 1f
                                }
                                else -> {
                                    // Covered: parallax to the left and dim.
                                    translationX = -depth * width * 0.25f
                                    alpha = 1f - 0.12f * depth
                                }
                            }
                        },
                ) {
                    content(key)
                }
            }
        }
    }
}
