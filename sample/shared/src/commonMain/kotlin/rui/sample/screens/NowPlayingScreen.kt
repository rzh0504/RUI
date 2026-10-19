package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.unit.dp
import rui.components.RuiButtonColors
import rui.components.RuiButtonSize
import rui.components.RuiIconButton
import rui.components.RuiKaraokeLyricsColors
import rui.components.RuiKaraokeLyricsView
import rui.components.RuiSlider
import rui.components.RuiSliderColors
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

private val DockSliderColors = RuiSliderColors(
    trackFill = Color.White,
    trackRest = Color.White.copy(alpha = 0.30f),
    thumb = Color.White,
    disabledTrackFill = Color.White,
    disabledTrackRest = Color.White.copy(alpha = 0.30f),
    disabledThumb = Color.White,
)

/**
 * The music block: a fully immersive Apple Music-inspired now-playing page
 * composed from Rui tokens. The artwork wash runs edge-to-edge behind the
 * status bar and the system navigation bar (status icons flip to light while
 * the page is on top); the glass catalog bar gives way to the page's own
 * chrome — a dismiss chevron, the album thumb, title and artist. The
 * karaoke lyrics fill the space between header and dock with the additive
 * glow, and the docked transport carries the progress between its time
 * labels plus previous / play / next. Previous/next rotate the two demo
 * tracks; tapping a lyric line seeks.
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
        // Page chrome: dismiss chevron, album thumb, titles.
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
            RuiIconButton(
                onClick = onBack,
                colors = DockButtonColors,
                shape = CircleShape,
            ) {
                RuiIcon(RuiIcons.ChevronDown, contentDescription = "Back to catalog")
            }
            Spacer(Modifier.width(RuiTheme.spacing.md))
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RuiTheme.shapes.sm)
                    .background(track.artwork),
            )
            Spacer(Modifier.width(RuiTheme.spacing.md))
            Column {
                RuiText(track.title, style = RuiTheme.typography.titleSm, color = Color.White)
                RuiText(
                    track.artist,
                    style = RuiTheme.typography.bodySm,
                    color = Color.White.copy(alpha = 0.6f),
                )
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
            RuiSlider(
                value = positionMs.toFloat() / track.durationMs,
                onValueChange = { positionMs = (it * track.durationMs).roundToInt() },
                colors = DockSliderColors,
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
                    RuiIcon(RuiIcons.SkipBack, contentDescription = "Previous track")
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
                    RuiIcon(RuiIcons.SkipForward, contentDescription = "Next track")
                }
            }
        }
    }
}
