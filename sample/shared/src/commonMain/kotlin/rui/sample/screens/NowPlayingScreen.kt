package rui.sample.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import rui.components.RuiButtonColors
import rui.components.RuiButtonSize
import rui.components.RuiIconButton
import rui.components.RuiKaraokeLyricsColors
import rui.components.RuiKaraokeLyricsView
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.utils.StatusBarAppearance
import rui.theme.RuiTheme
import kotlin.math.roundToInt

/**
 * Chrome of the immersive player page on the artwork wash: white icons that
 * dim into the artwork like Apple Music's player, instead of scoped content
 * tokens. Colors live with the block because the artwork layer carries the
 * palette, not the theme.
 */
private val DockButtonColors = RuiButtonColors(
    container = Color.Transparent,
    content = Color.White,
)

/**
 * The music block: a fully immersive Apple Music-inspired now-playing page
 * composed from Rui tokens. The artwork wash runs edge-to-edge behind the
 * status bar and the system navigation bar (status icons flip to light while
 * the page is on top); the glass catalog bar gives way to the page's own
 * chrome — album thumb, title and artist on the left, the dismiss chevron in
 * the top-right corner. The karaoke lyrics fill the space between header
 * and dock with the additive glow, and the docked transport carries a
 * scrubber-only progress line (no resting thumb, thickening with it under
 * the finger) between its time labels plus previous / play / next.
 * Previous/next rotate the two demo tracks; tapping a lyric line seeks.
 */
@Composable
public fun NowPlayingScreen(onBack: () -> Unit = {}) {
    var trackIndex by remember { mutableIntStateOf(0) }
    val track = DemoTracks[trackIndex.coerceIn(DemoTracks.indices)]
    var positionMs by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(true) }

    // Light system icons while the dark wash is behind the bars; the app
    // body's own StatusBarAppearance call re-applies the theme's appearance
    // when this page leaves composition.
    StatusBarAppearance(darkIcons = false)

    // The fake player's clock: one frame-time delta per frame, so the lyrics'
    // position provider observes a state that actually advances with
    // playback. Switching tracks rewinds through the rotate handlers.
    LaunchedEffect(playing, trackIndex) {
        var lastFrame = 0L
        while (playing) {
            withFrameNanos { frameTime ->
                if (lastFrame != 0L && positionMs < track.durationMs) {
                    positionMs = (positionMs + ((frameTime - lastFrame) / 1_000_000L).toInt())
                        .coerceAtMost(track.durationMs)
                }
                lastFrame = frameTime
            }
        }
    }

    // Fully immersive: the wash fills the window edge-to-edge — behind the
    // status bar above and the system navigation bar below; the insets only
    // lift the content, they never clip the artwork.
    Column(
        Modifier
            .fillMaxSize()
            .background(track.background),
    ) {
        // Page chrome: album thumb and titles on the left, the dismiss
        // chevron parked in the top-right corner.
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = RuiTheme.spacing.xl,
                    vertical = RuiTheme.spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RuiTheme.shapes.sm)
                    .background(track.artwork),
            )
            Column(Modifier.padding(start = RuiTheme.spacing.md).weight(1f)) {
                RuiText(track.title, style = RuiTheme.typography.titleSm, color = Color.White)
                RuiText(
                    track.artist,
                    style = RuiTheme.typography.bodySm,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
            RuiIconButton(
                onClick = onBack,
                colors = DockButtonColors,
                shape = CircleShape,
            ) {
                RuiIcon(RuiIcons.ChevronDown, contentDescription = "Back to catalog")
            }
        }

        // The lyrics own the space between header and dock; the view's
        // default anchor keeps the focused line clear of the dock's fade.
        RuiKaraokeLyricsView(
            lyrics = track.lyrics,
            currentPosition = { positionMs },
            onLineClicked = { line -> positionMs = line.start.coerceAtMost(track.durationMs) },
            colors = RuiKaraokeLyricsColors(text = Color.White),
            blendMode = BlendMode.Plus,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = RuiTheme.spacing.lg),
        )

        // Docked transport: progress between the time labels, controls
        // centered below — Apple Music proportions lifted over the system
        // navigation bar.
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = RuiTheme.spacing.xl)
                .padding(bottom = RuiTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TransportScrubber(
                fraction = { positionMs.toFloat() / track.durationMs },
                onScrub = { positionMs = (it * track.durationMs).roundToInt() },
                onScrubFinished = {},
            )
            Row(Modifier.fillMaxWidth()) {
                RuiText(
                    clockLabel(positionMs),
                    style = RuiTheme.typography.labelSm,
                    color = Color.White.copy(alpha = 0.6f),
                )
                Spacer(Modifier.weight(1f))
                RuiText(
                    clockLabel(track.durationMs),
                    style = RuiTheme.typography.labelSm,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
            Row(
                Modifier.padding(top = RuiTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiIconButton(
                    onClick = {
                        trackIndex = (trackIndex - 1).mod(DemoTracks.size)
                        positionMs = 0
                    },
                    colors = DockButtonColors,
                ) {
                    RuiIcon(
                        RuiIcons.SkipBack,
                        contentDescription = "Previous track",
                        size = 22.dp,
                    )
                }
                Spacer(Modifier.width(RuiTheme.spacing.lg))
                RuiIconButton(
                    onClick = {
                        if (!playing && positionMs >= track.durationMs) positionMs = 0
                        playing = !playing
                    },
                    size = RuiButtonSize.Lg,
                    shape = CircleShape,
                    colors = DockButtonColors,
                ) {
                    RuiIcon(
                        if (playing) RuiIcons.Pause else RuiIcons.Play,
                        contentDescription = if (playing) "Pause" else "Play",
                        size = 26.dp,
                    )
                }
                Spacer(Modifier.width(RuiTheme.spacing.lg))
                RuiIconButton(
                    onClick = {
                        trackIndex = (trackIndex + 1).mod(DemoTracks.size)
                        positionMs = 0
                    },
                    colors = DockButtonColors,
                ) {
                    RuiIcon(
                        RuiIcons.SkipForward,
                        contentDescription = "Next track",
                        size = 22.dp,
                    )
                }
            }
        }
    }
}

/**
 * Apple Music-style scrubber: a hairline track at rest that thickens and
 * grows a thumb only while the user scrubs — a player-page progress line,
 * not a slider control: no resting knob, jump-to-touch, live-following
 * drag. [fraction] is read per frame so the fill tracks playback; [onScrub]
 * fires on every drag delta (continuous seek).
 */
@Composable
private fun TransportScrubber(
    fraction: () -> Float,
    onScrub: (Float) -> Unit,
    onScrubFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragging by remember { mutableStateOf(false) }
    var widthPx by remember { mutableIntStateOf(0) }
    // Same spring forms RuiSlider uses: tracking while scrubbing, settle on
    // programmatic jumps (the two motion tokens of the slider itself).
    val thickness by animateFloatAsState(
        targetValue = if (dragging) 7f else 3f,
        animationSpec = if (dragging) RuiTheme.motion.tracking else RuiTheme.motion.settle,
    )
    val animatedFraction by animateFloatAsState(
        targetValue = fraction(),
        animationSpec = if (dragging) RuiTheme.motion.tracking else RuiTheme.motion.settle,
    )

    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = RuiTheme.dimens.touchTarget)
            .onSizeChanged { widthPx = it.width }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    dragging = true
                    onScrub(fractionAt(down.position.x, size.width))
                    horizontalDrag(down.id) { change ->
                        onScrub(fractionAt(change.position.x, size.width))
                        change.consume()
                    }
                    dragging = false
                    onScrubFinished()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val height = thickness.dp
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.30f)),
        )
        Box(
            Modifier
                .fillMaxWidth(animatedFraction)
                .height(height)
                .clip(CircleShape)
                .background(Color.White),
        )
        // The thumb only exists under the finger; the fill IS the progress.
        if (dragging && widthPx > 0) {
            val thumbPx = with(LocalDensity.current) { 14.dp.toPx() }
            Box(
                Modifier
                    .offset(x = with(LocalDensity.current) {
                        (animatedFraction * (widthPx - thumbPx)).toDp()
                    })
                    .size(14.dp)
                    .background(Color.White, CircleShape),
            )
        }
    }
}

/** The scrub fraction of a touch at [x] along a track of [widthPx] pixels. */
private fun fractionAt(x: Float, widthPx: Int): Float =
    if (widthPx > 0) (x / widthPx).coerceIn(0f, 1f) else 0f
