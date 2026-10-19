package rui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mocharealm.accompanist.lyrics.core.model.ISyncedLine
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import rui.theme.RuiTheme

/**
 * Colors of [RuiKaraokeLyricsView]. Lyrics default to the theme's [content]
 * color so the view stays legible on both appearance modes; music players
 * that render over album artwork pass a bright color of their own:
 *
 * ```
 * RuiKaraokeLyricsView(
 *     lyrics = lyrics,
 *     currentPosition = { position },
 *     colors = RuiKaraokeLyricsColors(text = Color.White),
 *     blendMode = BlendMode.Plus,
 * )
 * ```
 */
@Immutable
public data class RuiKaraokeLyricsColors(
    /** Base lyric color: text, translations and breathing dots. */
    public val text: Color,
)

/** Defaults of [RuiKaraokeLyricsView], resolved from Rui's tokens. */
public object RuiKaraokeLyricsDefaults {
    @Composable
    public fun colors(
        text: Color = RuiTheme.colors.content,
    ): RuiKaraokeLyricsColors = RuiKaraokeLyricsColors(
        text = text,
    )

    /** Main vocal style: the display title tier, unspecified line height. */
    @Composable
    public fun normalLineTextStyle(): TextStyle =
        RuiTheme.typography.titleXl.copy(
            lineHeight = TextUnit.Unspecified,
            textMotion = TextMotion.Animated,
        )

    /** Accompaniment/background-vocal style: one tier below the main vocals. */
    @Composable
    public fun accompanimentLineTextStyle(): TextStyle =
        RuiTheme.typography.titleLg.copy(
            lineHeight = TextUnit.Unspecified,
            fontWeight = FontWeight.Bold,
            textMotion = TextMotion.Animated,
        )

    /** Translation style between the main title and body tiers. */
    @Composable
    public fun translationTextStyle(): TextStyle =
        RuiTheme.typography.titleLg.copy(
            fontSize = 18.sp,
            lineHeight = TextUnit.Unspecified,
            fontWeight = FontWeight.Bold,
            textMotion = TextMotion.Animated,
        )

    /** Phonetic caption style; [TextMotion.Static] fixes the glyph grid. */
    @Composable
    public fun phoneticTextStyle(): TextStyle =
        normalLineTextStyle().copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textMotion = TextMotion.Static,
        )

    /**
     * Plain compositing keeps the view legible on page backgrounds of any
     * appearance; [BlendMode.Plus] is the artwork-overlay extra, paired
     * with a bright [RuiKaraokeLyricsColors.text] on a dark container.
     */
    public fun blendMode(): BlendMode = BlendMode.SrcOver
}

/**
 * Apple Music-style synced lyrics, wrapping Accompanist's
 * [KaraokeLyricsView](https://accompanist.docs.mocharealm.com/lyrics-ui/):
 * per-syllable karaoke timing, multi-voice duet alignment, accompaniment
 * (background vocal) rows, translations and phonetics, breathing dots during
 * intros and interludes, blur-by-distance focus and edge fades.
 *
 * Playback feeds the view through [currentPosition] in milliseconds. The
 * provider must read state that changes during playback (a state value
 * updated by the player or a frame clock) — a plain getter that returns the
 * same value each composition never advances the lyrics or requests redraws:
 *
 * ```
 * var position by remember { mutableIntStateOf(0) }
 * LaunchedEffect(playing) {
 *     var lastFrame = 0L
 *     while (playing) {
 *         withFrameNanos { now ->
 *             if (lastFrame != 0L) {
 *                 position = minOf(position + ((now - lastFrame) / 1_000_000L).toInt(), duration)
 *             }
 *             lastFrame = now
 *         }
 *     }
 * }
 *
 * RuiKaraokeLyricsView(
 *     lyrics = lyrics,
 *     currentPosition = { position },
 *     onLineClicked = { line -> player.seek(line.start) },
 * )
 * ```
 *
 * Gestures: tapping a line scrolls to it and reports it via
 * [onLineClicked]; long-pressing reports it via [onLinePressed] for actions
 * like copy or share. A manual swipe pauses auto-follow for
 * Accompanist's default 2.5 s before re-engaging.
 *
 * The view fills its [modifier]; give it a bounded height sized to the
 * container (a player page, a frosted panel or the screen). Native-device
 * platforms render with Accompanist's PreparedText engine; the browser and
 * iOS simulator targets fall back to a Compose-text renderer with the same
 * line states, alignment, translations and per-syllable fill.
 */
@Composable
public fun RuiKaraokeLyricsView(
    lyrics: SyncedLyrics,
    currentPosition: () -> Int,
    modifier: Modifier = Modifier,
    onLineClicked: (ISyncedLine) -> Unit = {},
    onLinePressed: (ISyncedLine) -> Unit = {},
    colors: RuiKaraokeLyricsColors = RuiKaraokeLyricsDefaults.colors(),
    normalLineTextStyle: TextStyle = RuiKaraokeLyricsDefaults.normalLineTextStyle(),
    accompanimentLineTextStyle: TextStyle = RuiKaraokeLyricsDefaults.accompanimentLineTextStyle(),
    translationTextStyle: TextStyle = RuiKaraokeLyricsDefaults.translationTextStyle(),
    phoneticTextStyle: TextStyle = RuiKaraokeLyricsDefaults.phoneticTextStyle(),
    blendMode: BlendMode = RuiKaraokeLyricsDefaults.blendMode(),
    showTranslation: Boolean = true,
    showPhonetic: Boolean = true,
) {
    PlatformKaraokeLyricsView(
        lyrics = lyrics,
        currentPosition = currentPosition,
        onLineClicked = onLineClicked,
        onLinePressed = onLinePressed,
        colors = colors,
        normalLineTextStyle = normalLineTextStyle,
        accompanimentLineTextStyle = accompanimentLineTextStyle,
        translationTextStyle = translationTextStyle,
        phoneticTextStyle = phoneticTextStyle,
        blendMode = blendMode,
        showTranslation = showTranslation,
        showPhonetic = showPhonetic,
        useBlurEffect = true,
        modifier = modifier,
    )
}

/**
 * The platform hook behind the public wrapper. Android, desktop JVM and iOS
 * devices bind it to Accompanist's renderer; the browser and iOS simulator
 * bind it to the Compose-text fallback, which mirrors the visible line
 * states without the SDF glyph pipeline.
 */
@Composable
internal expect fun PlatformKaraokeLyricsView(
    lyrics: SyncedLyrics,
    currentPosition: () -> Int,
    onLineClicked: (ISyncedLine) -> Unit,
    onLinePressed: (ISyncedLine) -> Unit,
    colors: RuiKaraokeLyricsColors,
    normalLineTextStyle: TextStyle,
    accompanimentLineTextStyle: TextStyle,
    translationTextStyle: TextStyle,
    phoneticTextStyle: TextStyle,
    blendMode: BlendMode,
    showTranslation: Boolean,
    showPhonetic: Boolean,
    useBlurEffect: Boolean,
    modifier: Modifier,
)
