package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import kotlin.math.roundToInt
import rui.components.RuiDivider
import rui.components.RuiIconButton
import rui.components.RuiKaraokeLyricsColors
import rui.components.RuiKaraokeLyricsView
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSlider
import rui.components.RuiSwitch
import rui.components.RuiButtonVariant
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/**
 * The lyrics demo: a token-shaped dark artwork panel holds the view while the
 * card above it runs the fake player — a frame-driven clock feeding the view's
 * position provider, a play/pause toggle, and a labeled transport slider.
 * Caption toggles sit below. The track data lives in [DemoTracks].
 */
@Composable
public fun LyricsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val track = DemoTracks.first()
    var positionMs by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(false) }
    var showPhonetic by remember { mutableStateOf(false) }

    // The fake player's clock: one frame-time delta per frame, so
    // RuiKaraokeLyricsView's provider observes state that actually advances
    // with playback (the contract the native renderer follows).
    LaunchedEffect(playing) {
        var lastFrame = 0L
        while (playing) {
            withFrameNanos { frameTime ->
                if (lastFrame != 0L && positionMs < track.durationMs) {
                    positionMs =
                        (positionMs + ((frameTime - lastFrame) / 1_000_000L).toInt())
                            .coerceAtMost(track.durationMs)
                    // Natural completion stops the fake player, so the Play
                    // icon returns and one press restarts the track.
                    if (positionMs >= track.durationMs) playing = false
                }
                lastFrame = frameTime
            }
        }
    }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Player")
        RuiListGroup {
            Row(
                Modifier.padding(
                    horizontal = RuiTheme.spacing.lg,
                    vertical = RuiTheme.spacing.md,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiIconButton(
                    onClick = {
                        if (!playing && positionMs >= track.durationMs) positionMs = 0
                        playing = !playing
                    },
                    variant = RuiButtonVariant.Primary,
                ) {
                    RuiIcon(
                        imageVector = if (playing) RuiIcons.Pause else RuiIcons.Play,
                        contentDescription = if (playing) "Pause" else "Play",
                    )
                }
                Column(Modifier.padding(start = RuiTheme.spacing.md)) {
                    RuiText(track.title, style = RuiTheme.typography.titleSm)
                    RuiText(
                        "Demo track · ${clockLabel(track.durationMs)}",
                        style = RuiTheme.typography.bodySm,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
                Spacer(Modifier.weight(1f))
                RuiText(
                    "${clockLabel(positionMs)} / ${clockLabel(track.durationMs)}",
                    style = RuiTheme.typography.labelMd,
                    color = RuiTheme.colors.contentMuted,
                )
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            Row(
                Modifier.padding(
                    horizontal = RuiTheme.spacing.lg,
                    vertical = RuiTheme.spacing.md,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiText(
                    clockLabel(positionMs),
                    style = RuiTheme.typography.labelSm,
                    color = RuiTheme.colors.contentMuted,
                )
                RuiSlider(
                    value = positionMs.toFloat() / track.durationMs,
                    onValueChange = { positionMs = (it * track.durationMs).roundToInt() },
                    onValueChangeFinished = {
                        // Releasing on the tail ends the demo, like natural
                        // completion — the clock's advance guard alone can
                        // never observe a seek that already jumped there.
                        if (positionMs >= track.durationMs) playing = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = RuiTheme.spacing.md),
                )
                RuiText(
                    clockLabel(track.durationMs),
                    style = RuiTheme.typography.labelSm,
                    color = RuiTheme.colors.contentMuted,
                )
            }
        }

        // The artwork panel: clipped to the same token shape as the cards
        // above it, dark album gradient, lyrics in white with the additive
        // glow the view is designed for.
        Box(
            Modifier
                .fillMaxWidth()
                .height(440.dp)
                .clip(RuiTheme.shapes.md)
                .background(track.background),
        ) {
            RuiKaraokeLyricsView(
                lyrics = track.lyrics,
                currentPosition = { positionMs },
                onLineClicked = { line -> positionMs = line.start },
                colors = RuiKaraokeLyricsColors(text = Color.White),
                blendMode = BlendMode.Plus,
                showTranslation = showTranslation,
                showPhonetic = showPhonetic,
                modifier = Modifier.fillMaxSize(),
            )
        }

        RuiText(
            "Tap a line to seek · long-press to act",
            style = RuiTheme.typography.labelMd,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.padding(start = RuiTheme.spacing.lg),
        )

        SectionLabel("Captions")
        RuiListGroup {
            RuiListItem(
                title = "Translation",
                trailing = { RuiSwitch(checked = showTranslation, onCheckedChange = { showTranslation = it }) },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Phonetic",
                trailing = { RuiSwitch(checked = showPhonetic, onCheckedChange = { showPhonetic = it }) },
            )
        }
    }
}
