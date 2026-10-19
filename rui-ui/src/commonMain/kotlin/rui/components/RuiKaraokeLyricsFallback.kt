package rui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mocharealm.accompanist.lyrics.core.model.ISyncedLine
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeAlignment
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeSyllable
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import com.mocharealm.accompanist.lyrics.core.model.synced.UncheckedSyncedLine
import kotlinx.coroutines.launch
import rui.foundation.RuiText
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Top padding that parks the active line below the viewport's head. */
private val FallbackTopPadding = 72.dp

/** Bottom padding letting follow-up lines pre-render past the anchor. */
private val FallbackBottomPadding = 160.dp

/** Gap between rows, mirroring Accompanist's default item spacing. */
private val FallbackItemSpacing = 16.dp

/** Horizontal indent of accompaniment rows, per the native renderer. */
private val AccompanimentIndent = 16.dp

/** Breathing dots share the row inset, riding where the next line lands. */
private val BreathingDotsIndent = AccompanimentIndent

/** Diameter of one breathing dot. */
private val BreathingDotSize = 8.dp

/** Gap between the three breathing dots. */
private val BreathingDotSpacing = 10.dp

/** Rest alpha of a breathing dot at the low end of its sine wave. */
private const val BreathingDotAlphaFloor = 0.3f

/** Duration of one full breathing cycle of a dot, in milliseconds. */
private const val BreathingCycleMs = 1600

/** Alpha of rows outside the focus window. */
internal const val UnfocusedRowAlpha = 0.35f

/** Alpha of accompaniment rows relative to their main contact. */
internal const val AccompanimentRowAlpha = 0.75f

/** Extra alpha of translation and phonetic captions under their row. */
internal const val CaptionRowAlpha = 0.6f

/** Resume delay after a manual swipe, mirroring the native view. */
private val FallbackFollowResumeDelay = 2.5.seconds

/**
 * Decision math of the fallback renderer, kept pure so it can be unit-tested
 * where the composables themselves cannot run.
 */
internal object FallbackKaraokeModel {
    /**
     * Alpha of a syllable at [progress] (0..1): the "sung" portion reads at
     * full [RuiKaraokeLyricsColors.text] alpha, the unlit head sits at
     * Accompanist's unlit factor, and both ends clamp to the constants.
     */
    public fun syllableHighlightAlpha(progress: Float): Float =
        UnfocusedRowAlpha + (1f - UnfocusedRowAlpha) * progress.coerceIn(0f, 1f)

    /**
     * Every line whose time range contains [time], ascending — duet rows and
     * simultaneous accompaniment all read as focused. Empty outside the
     * song's timed span.
     */
    public fun activeIndices(lyrics: SyncedLyrics, time: Int): IntArray =
        lyrics.getCurrentAllHighlightLineIndicesByTime(time).toIntArray()

    /**
     * The item to anchor at the viewport top, or `-1` to keep following the
     * current position (interludes only drift the clock). During a leading
     * intro and after the song the target is the first/last item so the
     * viewport parks itself instead of trailing the cursor off-list.
     */
    public fun targetIndex(lyrics: SyncedLyrics, time: Int): Int {
        val indices = activeIndices(lyrics, time)
        if (indices.isNotEmpty()) return indices.first()

        val first = lyrics.lines.firstOrNull() ?: return -1
        return when {
            time <= first.start -> 0
            lyrics.lines.last().end < time -> lyrics.lines.lastIndex
            else -> -1
        }
    }

    /**
     * True while playback sits in an intro or an interlude: no line is
     * active and lyrics are still ahead of the cursor — the window where
     * the fallback shows its breathing dots. The quiet tail after the song
     * is not a breathing window.
     */
    public fun isIntroOrInterlude(lyrics: SyncedLyrics, time: Int): Boolean {
        val lines = lyrics.lines
        if (lines.isEmpty()) return false
        return time < lines.last().end && activeIndices(lyrics, time).isEmpty()
    }
}

/** Manual-scroll pause window of the fallback follower. */
@Stable
private class FallbackFollowWindow {
    var gestureAt: TimeMark? = null
        private set

    fun noteManualGesture() {
        gestureAt = TimeSource.Monotonic.markNow()
    }

    /** True once the swipe cooldown has fully elapsed (or never started). */
    fun canFollow(): Boolean {
        val mark = gestureAt ?: return true
        return mark.elapsedNow() >= FallbackFollowResumeDelay
    }
}

/**
 * Pure-Compose stand-in for Accompanist's PreparedText engine. It renders
 * every row the data model defines — per-syllable karaoke fill, duet
 * alignment, accompaniment lines, translations, phonetics — with standard
 * Compose text, and follows playback with the same manual-scroll pause
 * window the native view uses. It backs the browser and iOS simulator
 * targets, where the Accompanist artifact cannot resolve.
 */
@Composable
internal fun FallbackKaraokeLyricsView(
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
    modifier: Modifier,
) {
    val lines = remember(lyrics) { lyrics.lines }
    val provider = rememberUpdatedState(currentPosition)
    val position = remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val followWindow = remember(lyrics) { FallbackFollowWindow() }

    // One frame clock feeds the karaoke fill and the scroll follower, so
    // both observe the same playback value; the caller's own state updates
    // only when the value actually changes (a paused player stops advancing).
    LaunchedEffect(listState, followWindow) {
        var programmaticScroll = false
        var lastAnchor = -1
        while (true) {
            withFrameNanos { }
            val now = provider.value()
            position.value = now
            val scrolling = listState.isScrollInProgress
            if (scrolling && !programmaticScroll) {
                // Invalidate the last auto anchor: a stale baseline equal to
                // the current target would block re-following that same line
                // once the cooldown expires — the documented resume behavior
                // returns to the active line even when it never changed.
                followWindow.noteManualGesture()
                lastAnchor = -1
            }
            if (!scrolling && followWindow.canFollow()) {
                val anchor = FallbackKaraokeModel.targetIndex(lyrics, now)
                if (anchor >= 0 && anchor != lastAnchor) {
                    lastAnchor = anchor
                    programmaticScroll = true
                    try {
                        listState.animateScrollToItem(anchor)
                    } finally {
                        programmaticScroll = false
                    }
                }
            }
        }
    }

    val active by remember(lyrics) {
        derivedStateOf {
            // Set keeps derived-state equality content-based, so the visible
            // rows only recompose when the focus window actually changes.
            FallbackKaraokeModel.activeIndices(lyrics, position.value).toSet()
        }
    }
    // The intro/interlude state rides its own frame-independent derived
    // value: the dots show exactly when no line is active and lyrics are
    // still ahead of the cursor — and they return when scrubbing back.
    val breathing by remember(lyrics) {
        derivedStateOf { FallbackKaraokeModel.isIntroOrInterlude(lyrics, position.value) }
    }

    // The root carries the artwork-overlay request and clips its children:
    // the scrolling rows and the breathing dots share one blended layer,
    // so Plus / Screen glow like the native view applies to both.
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .graphicsLayer {
                if (blendMode != BlendMode.SrcOver) {
                    compositingStrategy = CompositingStrategy.Offscreen
                    this.blendMode = blendMode
                }
            },
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = FallbackTopPadding, bottom = FallbackBottomPadding),
            verticalArrangement = Arrangement.spacedBy(FallbackItemSpacing),
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
        ) {
            items(count = lines.size, key = { it }) { index ->
                val line = lines[index]
                FallbackLineItem(
                    line = line,
                    active = index in active,
                    colors = colors,
                    normalLineTextStyle = normalLineTextStyle,
                    accompanimentLineTextStyle = accompanimentLineTextStyle,
                    translationTextStyle = translationTextStyle,
                    phoneticTextStyle = phoneticTextStyle,
                    showTranslation = showTranslation,
                    showPhonetic = showPhonetic,
                    position = { position.value },
                    clicked = {
                        scope.launch { listState.animateScrollToItem(index) }
                        onLineClicked(line)
                    },
                    pressed = { onLinePressed(line) },
                )
            }
        }
        if (breathing) {
            // Dots park at the viewport head — the anchor point where the
            // next line lands once the follower re-engages.
            BreathingDots(
                color = colors.text,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = FallbackTopPadding, start = BreathingDotsIndent),
            )
        }
    }
}

/**
 * One line: the type-specific row, its nested accompaniment lines, then the
 * phonetic and translation captions; tapping seeks, long-pressing acts.
 */
@Composable
private fun FallbackLineItem(
    line: ISyncedLine,
    active: Boolean,
    colors: RuiKaraokeLyricsColors,
    normalLineTextStyle: TextStyle,
    accompanimentLineTextStyle: TextStyle,
    translationTextStyle: TextStyle,
    phoneticTextStyle: TextStyle,
    showTranslation: Boolean,
    showPhonetic: Boolean,
    position: () -> Int,
    clicked: () -> Unit,
    pressed: () -> Unit,
) {
    val rightAligned = line is KaraokeLine && line.alignment == KaraokeAlignment.End
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(line) {
                detectTapGestures(
                    onTap = { clicked() },
                    onLongPress = { pressed() },
                )
            },
        horizontalAlignment = if (rightAligned) Alignment.End else Alignment.Start,
    ) {
        val textAlign = if (rightAligned) TextAlign.End else TextAlign.Start
        when (line) {
            is KaraokeLine.MainKaraokeLine -> {
                MainVocalRow(line, active, colors, normalLineTextStyle, textAlign, position)
                line.accompanimentLines?.forEach { accompaniment ->
                    // Nested vocals ride the same timed path as standalone
                    // accompaniment rows: syllable fill while focused, the
                    // dimmed read otherwise.
                    if (active) {
                        KaraokeProgressText(
                            syllables = accompaniment.syllables,
                            baseStyle = accompanimentLineTextStyle,
                            baseColor = colors.text.copy(alpha = AccompanimentRowAlpha),
                            textAlign = textAlign,
                            position = position,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AccompanimentIndent),
                        )
                    } else {
                        RuiText(
                            text = accompaniment.syllables.joinToString("") { it.content },
                            style = accompanimentLineTextStyle,
                            color = colors.text.copy(alpha = AccompanimentRowAlpha * UnfocusedRowAlpha),
                            textAlign = textAlign,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AccompanimentIndent),
                        )
                    }
                }
                Caption(line.phonetic, showPhonetic, phoneticTextStyle, colors.text, active, textAlign)
                Caption(line.translation, showTranslation, translationTextStyle, colors.text, active, textAlign)
            }

            is KaraokeLine.AccompanimentKaraokeLine -> {
                if (active) {
                    KaraokeProgressText(
                        syllables = line.syllables,
                        baseStyle = accompanimentLineTextStyle,
                        baseColor = colors.text.copy(alpha = AccompanimentRowAlpha),
                        textAlign = textAlign,
                        position = position,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AccompanimentIndent),
                    )
                } else {
                    RuiText(
                        text = line.syllables.joinToString("") { it.content },
                        style = accompanimentLineTextStyle,
                        color = colors.text.copy(alpha = AccompanimentRowAlpha * UnfocusedRowAlpha),
                        textAlign = textAlign,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AccompanimentIndent),
                    )
                }
                Caption(line.phonetic, showPhonetic, phoneticTextStyle, colors.text, active, textAlign)
                Caption(line.translation, showTranslation, translationTextStyle, colors.text, active, textAlign)
            }

            else -> {
                val synced = line as? SyncedLine ?: (line as? UncheckedSyncedLine)?.toSyncedLine()
                if (synced != null) {
                    RuiText(
                        text = synced.content,
                        style = normalLineTextStyle,
                        color = colors.text.copy(alpha = if (active) 1f else UnfocusedRowAlpha),
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Caption(synced.phonetic, showPhonetic, phoneticTextStyle, colors.text, active, textAlign)
                    Caption(synced.translation, showTranslation, translationTextStyle, colors.text, active, textAlign)
                }
            }
        }
    }
}

/** The main-vocal row: syllable fill when focused, dimmed whole-line text otherwise. */
@Composable
private fun MainVocalRow(
    line: KaraokeLine.MainKaraokeLine,
    active: Boolean,
    colors: RuiKaraokeLyricsColors,
    style: TextStyle,
    textAlign: TextAlign,
    position: () -> Int,
) {
    if (active) {
        KaraokeProgressText(
            syllables = line.syllables,
            baseStyle = style,
            baseColor = colors.text,
            textAlign = textAlign,
            position = position,
        )
    } else {
        RuiText(
            text = line.syllables.joinToString("") { it.content },
            style = style,
            color = colors.text.copy(alpha = UnfocusedRowAlpha),
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** A syllable-timed row painting per-syllable alpha ramps on [baseColor]. */
@Composable
private fun KaraokeProgressText(
    syllables: List<KaraokeSyllable>,
    baseStyle: TextStyle,
    baseColor: Color,
    textAlign: TextAlign,
    position: () -> Int,
    modifier: Modifier = Modifier,
) {
    // The annotated string rebuilds per playback frame of the focused row
    // only — unfocused rows carry plain text and skip this path entirely.
    val text = buildAnnotatedString {
        var cursor = 0
        for (syllable in syllables) {
            val start = cursor
            append(syllable.content)
            cursor += syllable.content.length
            addStyle(
                SpanStyle(
                    // Scale, don't replace, the base color's own alpha: an
                    // accompaniment row's caller hands a pre-dimmed color,
                    // and its highlight must keep that opacity.
                    color = baseColor.copy(
                        alpha = baseColor.alpha * FallbackKaraokeModel.syllableHighlightAlpha(
                            syllable.progress(position()),
                        ),
                    ),
                ),
                start,
                cursor,
            )
        }
    }
    BasicText(
        text = text,
        // Per-syllable spans carry the color; the base style stays a shape
        // template (size, weight, family), with alignment per the row.
        style = baseStyle.copy(textAlign = textAlign),
        modifier = modifier,
    )
}

/** Phonetic/translation caption under its row, honoring [show]. */
@Composable
private fun Caption(
    text: String?,
    show: Boolean,
    style: TextStyle,
    color: Color,
    active: Boolean,
    textAlign: TextAlign,
) {
    if (!show || text == null) return
    RuiText(
        text = text,
        style = style,
        color = color.copy(alpha = CaptionRowAlpha * if (active) 1f else UnfocusedRowAlpha),
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * The native view's intro/interlude state: three dots breathing in a
 * staggered sine wave, riding [RuiKaraokeLyricsColors.text] like the lyric
 * rows and dimming rather than vanishing between waves. The dots scale
 * with their brightness so the wave reads as a compositing layer, not as
 * blinking rectangles.
 */
@Composable
private fun BreathingDots(color: Color, modifier: Modifier = Modifier) {
    val cycle = rememberInfiniteTransition(label = "ruiBreathing")
    val phase by cycle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = BreathingCycleMs, easing = LinearEasing),
        ),
        label = "phase",
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BreathingDotSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            // Each dot trails its neighbor by a third of the cycle, so one
            // crest travels across the row without a jump at the wrap.
            val t = (phase - index / 3f + 3f) % 1f
            val lift = (sin(2f * PI.toFloat() * t) + 1f) / 2f
            Box(
                Modifier
                    .size(BreathingDotSize)
                    .graphicsLayer {
                        alpha = BreathingDotAlphaFloor + (1f - BreathingDotAlphaFloor) * lift
                        scaleX = 0.6f + 0.4f * lift
                        scaleY = scaleX
                    }
                    .background(color, CircleShape),
            )
        }
    }
}
