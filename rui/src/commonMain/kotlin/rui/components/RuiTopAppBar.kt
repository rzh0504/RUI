package rui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import rui.foundation.BlurBackdrop
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.blurSurface
import rui.foundation.platform.isBlurSupported
import rui.theme.RuiTheme
import kotlin.math.PI
import kotlin.math.abs

/**
 * The app bar: a small centered-title bar, or a large-title variant for entry
 * screens. The bar shares the page [RuiTheme.colors.background] color — at
 * rest it merges with the screen, and scrolled content is only revealed
 * through the frosted blur (miuix's unified-bar design). With a [backdrop]
 * the bar renders as frosted glass over scrolling content; without one it is
 * an opaque page-background strip. No hairline — the blur edge separates the
 * bar from the content, as in miuix/iOS.
 *
 * Pass a [scrollBehavior] (from [rememberRuiScrollBehavior]) for the iOS-style
 * collapsing large title: the large title shows by default and, as the content
 * scrolls up, fades out while a small centered title pops into the bar and the
 * bar shrinks down to its 52dp strip. Attach the behavior's
 * `nestedScrollConnection` to the scrolling content — see the overload notes
 * below for where it belongs relative to overscroll modifiers.
 *
 * Place inside [RuiScaffold]'s `topBar` slot; content that scrolls under the
 * bar is what makes the glass readable.
 */
@Composable
public fun RuiTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    largeTitle: Boolean = false,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    backdrop: BlurBackdrop? = null,
    scrollBehavior: RuiScrollBehavior? = null,
) {
    val colors = RuiTheme.colors
    val backLabel = RuiTheme.strings.back

    val barBackground = if (backdrop != null && isBlurSupported) {
        Modifier.blurSurface(
            backdrop = backdrop,
            radius = 32.dp,
            tint = colors.background.copy(alpha = 0.65f),
        )
    } else {
        Modifier.background(colors.background)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(barBackground)
            .statusBarsPadding()
            // The bar's strip swallows taps, like the bottom bars — content
            // scrolling beneath it must not react to taps on the chrome.
            .pointerInput(Unit) {
                detectTapGestures { }
            },
    ) {
        when {
            scrollBehavior != null -> CollapsingTopAppBarLayout(
                title = title,
                largeTitle = title,
                showBack = showBack,
                onBack = onBack,
                actions = actions,
                scrollBehavior = scrollBehavior,
            )
            largeTitle -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = RuiTheme.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showBack) {
                        RuiIconButton(
                            onClick = onBack,
                            variant = RuiButtonVariant.Plain,
                            modifier = Modifier.padding(end = RuiTheme.spacing.xs),
                        ) {
                            RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = backLabel)
                        }
                    }
                    RuiText(
                        text = title,
                        style = RuiTheme.typography.titleXl,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    actions()
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .padding(horizontal = RuiTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (showBack) {
                            RuiIconButton(onClick = onBack, variant = RuiButtonVariant.Plain) {
                                RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = backLabel)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        actions()
                    }
                    RuiText(
                        text = title,
                        style = RuiTheme.typography.titleMd,
                        modifier = Modifier.align(Alignment.Center),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Scroll state of the collapsing large-title top app bar.
 *
 * [heightOffsetLimit] is the (negative) pixel distance the bar can collapse —
 * written by the bar layout from the measured large-title height.
 * [heightOffset] is the current collapse offset, coerced into
 * `[heightOffsetLimit, 0]`; [collapsedFraction] maps it to `0..1`.
 */
@Stable
public class RuiTopAppBarState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
) {
    /** The pixel limit the bar is allowed to collapse by (always <= 0). */
    public var heightOffsetLimit: Float = initialHeightOffsetLimit

    /** The current collapse offset in pixels, coerced into `[heightOffsetLimit, 0]`. */
    public var heightOffset: Float
        get() = _heightOffset.floatValue
        public set(newOffset) {
            _heightOffset.floatValue = newOffset.coerceIn(minimumValue = heightOffsetLimit, maximumValue = 0f)
        }

    /** `0.0` = fully expanded large title, `1.0` = fully collapsed bar. */
    public val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) heightOffset / heightOffsetLimit else 0f

    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)

    public companion object {
        /** The default [Saver] implementation for [RuiTopAppBarState]. */
        public val Saver: Saver<RuiTopAppBarState, *> =
            listSaver(
                save = { listOf(it.heightOffsetLimit, it.heightOffset) },
                restore = {
                    RuiTopAppBarState(
                        initialHeightOffsetLimit = it[0],
                        initialHeightOffset = it[1],
                    )
                },
            )
    }
}

/** Remembers a [RuiTopAppBarState] across recompositions (and process death). */
@Composable
public fun rememberRuiTopAppBarState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
): RuiTopAppBarState = rememberSaveable(saver = RuiTopAppBarState.Saver) {
    RuiTopAppBarState(initialHeightOffsetLimit, initialHeightOffset)
}

/**
 * Drives a collapsing [RuiTopAppBar] from the scroll position of the content:
 * scrolling up collapses the bar first (exit-until-collapsed), scrolling down
 * expands it back, and a fling settles to fully expanded or collapsed.
 *
 * Attach `Modifier.nestedScroll(behavior.nestedScrollConnection)` to the
 * scrolling content — above any overscroll modifier, so the bar reacts before
 * the content bounces.
 */
@Stable
public interface RuiScrollBehavior {
    /** The state this behavior reads and writes. */
    public val state: RuiTopAppBarState

    /** Snap animation to fully expanded/collapsed after a drag or fling. */
    public val snapAnimationSpec: AnimationSpec<Float>?

    /** Decay animation that carries a fling into the bar collapse/expand. */
    public val flingAnimationSpec: DecayAnimationSpec<Float>?

    /** Attach with [androidx.compose.ui.input.nestedscroll.nestedScroll] on the content. */
    public val nestedScrollConnection: NestedScrollConnection
}

/**
 * Remembers a [RuiScrollBehavior] backed by [state].
 *
 * @param canScroll whether scroll events are currently handled by the bar;
 *   return `false` to keep the bar static while, say, a sub-list handles
 *   scrolling.
 */
@Composable
public fun rememberRuiScrollBehavior(
    state: RuiTopAppBarState = rememberRuiTopAppBarState(),
    canScroll: () -> Boolean = { true },
): RuiScrollBehavior {
    val flingAnimationSpec = rememberSplineBasedDecay<Float>()
    return remember(state, canScroll, flingAnimationSpec) {
        ExitUntilCollapsedScrollBehavior(
            state = state,
            canScroll = canScroll,
            snapAnimationSpec = spring(stiffness = 2500f),
            flingAnimationSpec = flingAnimationSpec,
        )
    }
}

private class ExitUntilCollapsedScrollBehavior(
    override val state: RuiTopAppBarState,
    val canScroll: () -> Boolean,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
) : RuiScrollBehavior {
    override val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Collapse first while scrolling up; don't intercept scrolling down.
            if (!canScroll() || available.y > 0) return Offset.Zero
            val prevHeightOffset = state.heightOffset
            state.heightOffset += available.y
            return if (prevHeightOffset != state.heightOffset) {
                // Mid-collapse — consume only the Y scroll.
                available.copy(x = 0f)
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (!canScroll()) return Offset.Zero

            if (available.y < 0f || consumed.y < 0f) {
                // Scrolling up: absorb whatever the content couldn't consume.
                val oldHeightOffset = state.heightOffset
                state.heightOffset += consumed.y
                return Offset(0f, state.heightOffset - oldHeightOffset)
            }

            if (available.y > 0f) {
                // Scrolling down past the top: expand the bar first.
                val oldHeightOffset = state.heightOffset
                state.heightOffset += available.y
                return Offset(0f, state.heightOffset - oldHeightOffset)
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val superConsumed = super.onPostFling(consumed, available)
            return superConsumed +
                settleAppBar(state, available.y, flingAnimationSpec, snapAnimationSpec)
        }
    }
}

/**
 * Settles a partially collapsed bar to fully expanded or collapsed: the fling
 * velocity first decays into the bar offset, then a snap animation finishes
 * whichever side of the midpoint it landed on.
 */
private suspend fun settleAppBar(
    state: RuiTopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
    // Already at an end (float-precision safe): nothing to settle.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(initialValue = 0f, initialVelocity = velocity).animateDecay(
            flingAnimationSpec,
        ) {
            val delta = value - lastValue
            val initialHeightOffset = state.heightOffset
            state.heightOffset = initialHeightOffset + delta
            val consumed = abs(initialHeightOffset - state.heightOffset)
            lastValue = value
            remainingVelocity = this.velocity
            // Stop early when rounding errors make consumption drift.
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
    }
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) 0f else state.heightOffsetLimit,
                animationSpec = snapAnimationSpec,
            ) {
                state.heightOffset = value
            }
        }
    }
    return Velocity(0f, velocity - remainingVelocity)
}

// Metrics of the collapsing layout, following miuix's TopAppBarDefaults.
private val CollapsedBarHeight = 52.dp
private val LargeTitlePadding = 26.dp

// Slack so the centred small title isn't butted against nav/actions.
private const val TitleWidthFraction = 0.9

/**
 * The collapsing large-title bar layout: a 52dp strip (back button, small
 * centered title, actions) plus the large title below it. All reads of the
 * scroll state happen in layout/draw phases, so scrolling never recomposes
 * the bar — only re-measures it.
 *
 * Adapted from the miuix project's TopAppBarLayout (Apache License 2.0, see
 * NOTICE); reduced by removing the subtitle/bottom-content slots and the
 * small-bar pinning interplay.
 */
@Composable
private fun CollapsingTopAppBarLayout(
    title: String,
    largeTitle: String,
    showBack: Boolean,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    scrollBehavior: RuiScrollBehavior?,
) {
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing
    val backLabel = RuiTheme.strings.back

    val scrolledOffset = remember(scrollBehavior) {
        { scrollBehavior?.state?.heightOffset ?: 0f }
    }
    val largeTitleAlpha = remember(scrollBehavior) {
        {
            val fraction = scrollBehavior?.state?.collapsedFraction ?: 0f
            1f - (fraction * 3f).coerceIn(0f, 1f)
        }
    }
    val updateHeightOffsetLimit = remember(scrollBehavior) {
        { height: Int ->
            scrollBehavior?.state?.let { state ->
                val limit = -height.toFloat()
                if (state.heightOffsetLimit != limit) state.heightOffsetLimit = limit
            }
            Unit
        }
    }

    // Boolean derivedStateOf invalidates only on flip → the spring fires once
    // per crossing, not per frame. The small title pops in once the large
    // title has faded (a third of the collapse).
    val smallTitleVisible by remember(scrollBehavior) {
        derivedStateOf {
            (scrollBehavior?.state?.collapsedFraction ?: 0f) * 3f >= 1f
        }
    }
    val smallTitleAlpha = remember { Animatable(if (smallTitleVisible) 1f else 0f) }
    val smallTitleTranslationY = remember { Animatable(if (smallTitleVisible) 0f else 20f) }

    LaunchedEffect(smallTitleVisible) {
        if (smallTitleVisible) {
            launch { smallTitleAlpha.animateTo(1f, titleSpring(0.3f)) }
            launch { smallTitleTranslationY.animateTo(0f, titleSpring(0.3f)) }
        } else {
            launch { smallTitleAlpha.animateTo(0f, titleSpring(0.15f)) }
            launch { smallTitleTranslationY.animateTo(20f, titleSpring(0.15f)) }
        }
    }

    val actionsRow =
        @Composable {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }

    Layout(
        {
            Box(
                Modifier
                    .layoutId("navigationIcon")
                    .padding(start = spacing.lg),
            ) {
                if (showBack) {
                    RuiIconButton(onClick = onBack, variant = RuiButtonVariant.Plain) {
                        RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = backLabel)
                    }
                }
            }
            Box(
                Modifier
                    .layoutId("title")
                    .padding(horizontal = LargeTitlePadding)
                    .graphicsLayer {
                        alpha = smallTitleAlpha.value
                        translationY = smallTitleTranslationY.value
                    },
            ) {
                RuiText(
                    text = title,
                    style = typography.titleMd,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier
                    .layoutId("actionIcons")
                    .padding(end = spacing.lg),
            ) {
                actionsRow()
            }
            Box(
                Modifier
                    .layoutId("largeTitle")
                    .padding(top = CollapsedBarHeight)
                    .padding(horizontal = LargeTitlePadding)
                    .graphicsLayer { alpha = largeTitleAlpha() },
            ) {
                Column(
                    modifier = Modifier
                        // The large title rides up with the collapsing bar,
                        // clipped at the strip's top edge.
                        .offset { IntOffset(0, scrolledOffset().fastRoundToInt()) }
                        .onSizeChanged { updateHeightOffsetLimit(it.height) },
                ) {
                    RuiText(text = largeTitle, style = typography.titleXl)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables
                .fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val actionIconsPlaceable =
            measurables
                .fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val maxTitleWidth =
            if (constraints.maxWidth == Constraints.Infinity) {
                constraints.maxWidth
            } else {
                (constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width)
                    .coerceAtLeast(0)
            }
        val titleMaxWidth =
            if (maxTitleWidth == Constraints.Infinity) {
                maxTitleWidth
            } else {
                (maxTitleWidth * TitleWidthFraction).fastRoundToInt()
            }

        val titlePlaceable =
            measurables
                .fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = titleMaxWidth, minHeight = 0))

        val largeTitlePlaceable =
            measurables
                .fastFirst { it.layoutId == "largeTitle" }
                .measure(
                    constraints.copy(
                        minWidth = 0,
                        minHeight = 0,
                        maxHeight = Constraints.Infinity,
                    ),
                )

        val collapsedHeight = CollapsedBarHeight.roundToPx()
        // The largeTitle Box carries top padding = collapsedHeight; subtract it
        // back for the pure expansion the scroll can eat away.
        val expansion = (largeTitlePlaceable.height - collapsedHeight).coerceAtLeast(0)
        val barHeight = if (expansion > 0) {
            val offset = scrolledOffset()
            val collapseFraction = if (offset.isNaN()) {
                0f
            } else {
                (abs(offset) / expansion.toFloat()).coerceIn(0f, 1f)
            }
            lerp(
                start = collapsedHeight,
                stop = collapsedHeight + expansion,
                fraction = 1f - collapseFraction,
            )
        } else {
            collapsedHeight
        }

        val verticalCenter = collapsedHeight / 2

        layout(constraints.maxWidth, barHeight) {
            navigationIconPlaceable.placeRelative(
                x = 0,
                y = verticalCenter - navigationIconPlaceable.height / 2,
            )

            // Small title, centred but nudged out of the nav/actions' way.
            var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
            if (baseX < navigationIconPlaceable.width) {
                baseX += (navigationIconPlaceable.width - baseX)
            } else if (baseX + titlePlaceable.width > constraints.maxWidth - actionIconsPlaceable.width) {
                baseX += ((constraints.maxWidth - actionIconsPlaceable.width) - (baseX + titlePlaceable.width))
            }
            titlePlaceable.placeRelative(
                x = baseX,
                y = verticalCenter - titlePlaceable.height / 2,
            )

            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = verticalCenter - actionIconsPlaceable.height / 2,
            )

            // Its internal top padding positions it below the 52dp strip; the
            // offset modifier slides it up while the bar collapses.
            largeTitlePlaceable.placeRelative(x = 0, y = 0)
        }
    }
}

// Critically damped spring by response time — Rui's take on miuix's folmeSpring.
private fun <T> titleSpring(response: Float): SpringSpec<T> =
    spring(dampingRatio = 1f, stiffness = ((2.0 * PI / response) * (2.0 * PI / response)).toFloat())
