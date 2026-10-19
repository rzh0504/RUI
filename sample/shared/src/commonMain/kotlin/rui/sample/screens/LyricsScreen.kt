package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeAlignment
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeSyllable
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import kotlin.math.roundToInt
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiDivider
import rui.components.RuiKaraokeLyricsColors
import rui.components.RuiKaraokeLyricsView
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiSlider
import rui.components.RuiSwitch
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/** Full length of the demo track the fake player runs the clock for. */
private const val TrackDurationMs: Int = 50_000

private fun phrase(text: String, start: Int, end: Int) = KaraokeSyllable(text, start, end)

/** `m:ss` readout of a position in milliseconds, manually padded for KMP. */
private fun clockLabel(ms: Int): String {
    val totalSeconds = ms / 1_000
    val minutes = totalSeconds / 60
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

/**
 * A small song that exercises the view's vocabulary: a phonetic + translation
 * main line, a main line carrying a nested accompaniment hook, plain synced
 * rows, and two right-aligned accompaniment interjections (the second one
 * syllable-timed). Times are deliberately gapped so the breathing dots and
 * the follow-vs-pause logic both appear while the track plays.
 */
private val DemoLyrics = SyncedLyrics(
    id = "demo",
    lines = listOf(
        KaraokeLine.MainKaraokeLine(
            syllables = listOf(
                phrase("晨", 5_000, 5_600),
                phrase("光", 5_600, 6_100),
                phrase("落", 6_100, 6_500),
                phrase("在", 6_500, 6_800),
                phrase("窗台", 6_800, 7_300),
            ),
            translation = "Morning light spills on the windowsill",
            alignment = KaraokeAlignment.Unspecified,
            start = 5_000,
            end = 7_300,
            phonetic = "chén guāng luò zài chuāng tái",
        ),
        KaraokeLine.MainKaraokeLine(
            syllables = listOf(
                phrase("揉", 8_000, 8_500),
                phrase("醒", 8_500, 9_000),
                phrase("沉", 9_000, 9_600),
                phrase("睡", 9_600, 10_100),
                phrase("的", 10_100, 10_400),
                phrase("台钟", 10_400, 10_800),
            ),
            translation = "Winding up the sleeping clock",
            alignment = KaraokeAlignment.Unspecified,
            start = 8_000,
            end = 10_800,
            accompanimentLines = listOf(
                KaraokeLine.AccompanimentKaraokeLine(
                    syllables = listOf(phrase("yeah", 8_600, 9_300), phrase("yeah", 9_600, 10_600)),
                    translation = null,
                    alignment = KaraokeAlignment.Unspecified,
                    start = 8_600,
                    end = 10_600,
                ),
            ),
        ),
        SyncedLine(
            content = "旧街的梧桐还没记住这阵风",
            translation = "The old street's poplar hasn't caught the wind",
            start = 11_400,
            end = 14_400,
        ),
        KaraokeLine.AccompanimentKaraokeLine(
            syllables = listOf(phrase("哦", 15_000, 16_700), phrase("哦——", 16_700, 18_000)),
            translation = null,
            alignment = KaraokeAlignment.End,
            start = 15_000,
            end = 18_000,
        ),
        KaraokeLine.MainKaraokeLine(
            syllables = listOf(
                phrase("把", 19_500, 20_000),
                phrase("黄", 20_000, 20_500),
                phrase("昏", 20_500, 21_000),
                phrase("拧", 21_000, 21_500),
                phrase("成", 21_500, 21_800),
                phrase("发条", 21_800, 22_300),
            ),
            translation = null,
            alignment = KaraokeAlignment.Unspecified,
            start = 19_500,
            end = 22_300,
            phonetic = "bǎ huáng hūn nǐng chéng fā tiáo",
        ),
        KaraokeLine.MainKaraokeLine(
            syllables = listOf(phrase("等", 23_000, 23_800), phrase("月亮过河", 23_800, 26_200)),
            translation = "Waiting for the moon to cross the river",
            alignment = KaraokeAlignment.Unspecified,
            start = 23_000,
            end = 26_200,
        ),
        SyncedLine(
            content = "有一段旋律还没有名字",
            translation = "There is a melody still unnamed",
            start = 27_500,
            end = 31_500,
            phonetic = "yǒu yī duàn xuán lǜ hái méi yǒu míng zi",
        ),
        KaraokeLine.AccompanimentKaraokeLine(
            syllables = listOf(
                phrase("la", 33_000, 34_100),
                phrase("la", 34_100, 35_200),
                phrase("la", 35_200, 36_500),
            ),
            translation = null,
            alignment = KaraokeAlignment.End,
            start = 33_000,
            end = 36_500,
        ),
        KaraokeLine.MainKaraokeLine(
            syllables = listOf(phrase("名字", 38_000, 38_900), phrase("写进", 38_900, 39_500), phrase("第二段", 39_500, 41_200)),
            translation = "The name finds the second verse",
            alignment = KaraokeAlignment.Unspecified,
            start = 38_000,
            end = 41_200,
        ),
        KaraokeLine.MainKaraokeLine(
            syllables = listOf(phrase("而风", 42_500, 43_500), phrase("还在", 43_500, 44_200), phrase("写落款", 44_200, 45_500)),
            translation = "While the wind keeps writing the signature",
            alignment = KaraokeAlignment.Unspecified,
            start = 42_500,
            end = 45_500,
        ),
    ),
)

/**
 * The lyrics demo: a dark player panel runs the view as a fake player — a
 * frame-driven clock playing the demo track — with transport controls and
 * the two caption toggles beneath it.
 */
@Composable
public fun LyricsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var positionMs by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }
    var showPhonetic by remember { mutableStateOf(true) }

    // The fake player's clock: one frame-time delta per frame, so
    // RuiKaraokeLyricsView's provider observes state that actually advances
    // with playback (the contract the native renderer follows).
    LaunchedEffect(playing) {
        var lastFrame = 0L
        while (playing) {
            withFrameNanos { frameTime ->
                if (lastFrame != 0L && positionMs < TrackDurationMs) {
                    positionMs =
                        (positionMs + ((frameTime - lastFrame) / 1_000_000L).toInt())
                            .coerceAtMost(TrackDurationMs)
                }
                lastFrame = frameTime
            }
        }
    }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Player")
        RuiListGroup {
            Column(
                Modifier.padding(
                    horizontal = RuiTheme.spacing.lg,
                    vertical = RuiTheme.spacing.md,
                ),
            ) {
                Row {
                    Column {
                        RuiText("Dawn by the window", style = RuiTheme.typography.titleMd)
                        RuiText(
                            "Demo track · 50 s",
                            style = RuiTheme.typography.bodySm,
                            color = RuiTheme.colors.contentMuted,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    RuiText(
                        "${clockLabel(positionMs)} / ${clockLabel(TrackDurationMs)}",
                        style = RuiTheme.typography.labelMd,
                        color = RuiTheme.colors.contentMuted,
                    )
                }

                Spacer(Modifier.height(RuiTheme.spacing.md))
                // Lyrics are designed to sit on dark artwork: the panel paints
                // a dim gradient and passes the white text + additive blend.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF101018), Color(0xFF1E1E2C))),
                        ),
                ) {
                    RuiKaraokeLyricsView(
                        lyrics = DemoLyrics,
                        currentPosition = { positionMs },
                        onLineClicked = { line -> positionMs = line.start },
                        colors = RuiKaraokeLyricsColors(text = Color.White),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Plus,
                        showTranslation = showTranslation,
                        showPhonetic = showPhonetic,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiSlider(
                    value = positionMs.toFloat() / TrackDurationMs,
                    onValueChange = { positionMs = (it * TrackDurationMs).roundToInt() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(RuiTheme.spacing.sm))
                Row {
                    RuiButton(
                        onClick = {
                            if (positionMs >= TrackDurationMs) positionMs = 0
                            playing = !playing
                        },
                        variant = RuiButtonVariant.Primary,
                    ) {
                        RuiText(if (playing) "Pause" else "Play")
                    }
                    Spacer(Modifier.weight(1f))
                    RuiText(
                        "Tap a line to seek · long-press to act",
                        style = RuiTheme.typography.labelMd,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
            }
        }

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

        SectionLabel("How it renders")
        RuiListGroup {
            RuiListItem(
                title = "Native renderer",
                subtitle = "Accompanist PreparedText engine on Android, desktop and iOS device",
            )
            RuiListItem(
                title = "Compose fallback",
                subtitle = "Per-syllable fill and the same follow rules on the browser and iOS simulator",
            )
        }
    }
}
