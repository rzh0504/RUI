package rui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.mocharealm.accompanist.lyrics.core.model.ISyncedLine
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeAlignment
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeSyllable
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine
import com.mocharealm.accompanist.lyrics.core.model.synced.UncheckedSyncedLine
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import rui.foundation.RuiText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
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

/** Three dots breathe, mirroring KaraokeBreathingDotsDefaults. */
private const val BreathingDotNumber = 3

/** Diameter of one breathing dot, per KaraokeBreathingDotsDefaults. */
private val BreathingDotSize = 16.dp

/** Gap between the breathing dots, per KaraokeBreathingDotsDefaults. */
private val BreathingDotSpacing = 12.dp

/** Enter ramp of the dots, per KaraokeBreathingDotsDefaults, in ms. */
private const val BreathingEnterMs = 3000

/** Pre-exit dip and rise, per KaraokeBreathingDotsDefaults, in ms. */
private const val BreathingDipMs = 3000

/** The still moment between the dip and the exit, in ms. */
private const val BreathingStillMs = 200

/** Exit ramp of the dots, per KaraokeBreathingDotsDefaults, in ms. */
private const val BreathingExitMs = 200

/** Smoothstep the slot height collapses along, in ms. */
private const val BreathingSlotRampMs = 220

/** Em-box estimate the dot slot rides on when line height is unspecified. */
private const val BreathingSlotFontScale = 1.35f

/** Main-vocal size the slot falls back to when the style is unspecified. */
private val BreathingSlotFallbackFontSize = 34.sp

/**
 * Interludes and intros at most this wide never breathe, mirroring the
 * native timeline's strict five-second gate — both engines show dots on
 * the same windows on platforms that render natively.
 */
internal const val BreathingMinGapMs = 5000

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
     * current position (short interludes only drift the clock). During a
     * leading intro and after the song the target is the first/last item so
     * the viewport parks itself instead of trailing the cursor off-list; a
     * wide interlude parks ahead on the line its dots are waiting for, as
     * the native follow does.
     */
    public fun targetIndex(lyrics: SyncedLyrics, time: Int): Int {
        val indices = activeIndices(lyrics, time)
        if (indices.isNotEmpty()) return indices.first()

        val first = lyrics.lines.firstOrNull() ?: return -1
        return when {
            time <= aggregateStart(first) -> 0
            aggregateEnd(lyrics.lines.last()) < time -> lyrics.lines.lastIndex
            else -> activeInterludeIndex(lyrics, time)
        }
    }

    /**
     * True while playback sits in a wide intro ahead of the first line —
     * the native timeline's `activeIntro`: the wait must exceed
     * [BreathingMinGapMs] strictly, and the tail before zero does not
     * breathe.
     */
    public fun isIntroBreathing(lyrics: SyncedLyrics, time: Int): Boolean {
        val first = lyrics.lines.firstOrNull() ?: return false
        val introEnd = aggregateStart(first)
        if (introEnd <= BreathingMinGapMs) return false
        return time in 0 until introEnd
    }

    /**
     * The item index a live interlude window waits for, or `-1` when none:
     * the gap from the previous line's aggregate end to the next line's
     * aggregate start must exceed [BreathingMinGapMs], and the window runs
     * from the instant the previous line ends — the native timeline's
     * `activeInterludeIndex`.
     */
    public fun activeInterludeIndex(lyrics: SyncedLyrics, time: Int): Int {
        val lines = lyrics.lines
        if (lines.size < 2) return -1
        var best = -1
        for (index in 1 until lines.size) {
            val start = aggregateEnd(lines[index - 1])
            val end = aggregateStart(lines[index])
            if (start <= time && time < end && end - start > BreathingMinGapMs) best = index
        }
        return best
    }

    /**
     * The breathing slot each item can lead with: item 0 during a wide
     * intro, item i above the line a wide interlude waits on. Windows the
     * native gate would skip are absent — both engines then keep the rows
     * flowing without dots.
     */
    public fun breathingSlots(lyrics: SyncedLyrics): Map<Int, BreathingSlot> {
        val lines = lyrics.lines
        if (lines.isEmpty()) return emptyMap()
        val slots = mutableMapOf<Int, BreathingSlot>()
        val introEnd = aggregateStart(lines.first())
        if (introEnd > BreathingMinGapMs) slots[0] = BreathingSlot(0, introEnd)
        for (index in 1 until lines.size) {
            val start = aggregateEnd(lines[index - 1])
            val end = aggregateStart(lines[index])
            if (end - start > BreathingMinGapMs) slots[index] = BreathingSlot(start, end)
        }
        return slots
    }

    /**
     * A line's playback start, pulled earlier by a leading nested
     * accompaniment exactly as the native timeline aggregates its bounds.
     */
    private fun aggregateStart(line: ISyncedLine): Int =
        minOf(
            line.start,
            (line as? KaraokeLine.MainKaraokeLine)?.accompanimentLines?.minOfOrNull {
                it.start
            } ?: line.start,
        )

    /** A line's playback end, pushed later by a trailing nested accompaniment. */
    private fun aggregateEnd(line: ISyncedLine): Int =
        maxOf(
            line.end,
            (line as? KaraokeLine.MainKaraokeLine)?.accompanimentLines?.maxOfOrNull {
                it.end
            } ?: line.end,
        )
}

/**
 * One breathing window: the intro ahead of the song's first line, or the
 * interlude above the line it waits for. The slot canvas reads the
 * playback envelope built from these bounds.
 */
internal data class BreathingSlot(val startMs: Int, val endMs: Int)

/**
 * Playback envelope of the breathing dots, ported from the native
 * renderer's PreparedBreathingDots phase boundaries: a smooth enter ramp, a
 * gentle cosine breathing, a pre-exit dip and rise, then a smooth exit —
 * with the row height riding quick smoothsteps at both ends of the window.
 * Everything is a pure function of playback time, so a paused player
 * freezes the dots exactly like the native layer does.
 */
internal class BreathingEnvelope(private val startMs: Int, endMs: Int) {
    private val duration = (endMs.toLong() - startMs).coerceAtLeast(1).toFloat()
    private val valid = endMs > startMs

    // Wide windows breathe at full phase length; short ones scale the
    // phases down proportionally so the envelope never overruns the gap.
    private val factor =
        (duration / (BreathingEnterMs + BreathingDipMs + BreathingStillMs + BreathingExitMs).toFloat())
            .coerceAtMost(1f)
    private val enterEnd = BreathingEnterMs * factor
    private val exitStart = duration - BreathingExitMs * factor
    private val stillStart = exitStart - BreathingStillMs * factor
    private val dipStart = stillStart - BreathingDipMs * factor
    private val breathingDuration = (dipStart - enterEnd).coerceAtLeast(0f)
    private val hasBreathing = breathingDuration > 16f
    private val halfCycles =
        (breathingDuration / 1500f)
            .roundToInt()
            .coerceAtLeast(1)
            .let { if (it % 2 == 0) it + 1 else it }
    private val period = 2f * breathingDuration / halfCycles
    private val dotSpan = (exitStart - enterEnd).coerceAtLeast(1f) / BreathingDotNumber

    fun elapsed(timeMs: Int): Float = (timeMs.toLong() - startMs).toFloat()

    fun scale(time: Float): Float =
        when {
            !valid -> 0f
            time < enterEnd -> smooth(time / enterEnd) * if (hasBreathing) 0.8f else 1f
            hasBreathing && time < dipStart ->
                0.9f - 0.1f * cos((time - enterEnd) / period * 2f * PI.toFloat())
            time < dipStart -> 1f
            time < stillStart ->
                0.8f + 0.2f * cos(
                    ((time - dipStart) / (stillStart - dipStart).coerceAtLeast(0.000001f))
                        .coerceIn(0f, 1f) * 2f * PI.toFloat(),
                )
            time < exitStart -> 1f
            else -> smooth((duration - time) / (duration - exitStart).coerceAtLeast(0.000001f))
        }

    fun alpha(time: Float): Float =
        when {
            !valid -> 0f
            time < enterEnd -> smooth(time / enterEnd)
            time < exitStart -> 1f
            else -> smooth((duration - time) / (duration - exitStart).coerceAtLeast(0.000001f))
        }

    fun dotAlpha(index: Int, time: Float): Float =
        0.2f + 0.8f * ((time - enterEnd - dotSpan * index) / dotSpan).coerceIn(0f, 1f)

    /** Slot height factor: invisible outside the window, one slot inside. */
    fun visibility(time: Float): Float =
        if (!valid || time < 0f || time >= duration) {
            0f
        } else {
            minOf(
                smooth(time / BreathingSlotRampMs),
                smooth((duration - time) / BreathingSlotRampMs),
            )
        }

    private fun smooth(value: Float): Float {
        val t = value.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
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
    // The auto scroll runs as a child job: a drag cancels that job through
    // the scroll mutex, and the sampling loop keeps running.
    LaunchedEffect(listState, followWindow) {
        var lastAnchor = -1
        var scrollJob: Job? = null
        while (true) {
            withFrameNanos { }
            val now = provider.value()
            position.value = now
            val scrolling = listState.isScrollInProgress
            val autoScrolling = scrollJob?.isActive == true
            if (scrolling && !autoScrolling) {
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
                    scrollJob?.cancel()
                    scrollJob = launch {
                        listState.animateScrollToItem(anchor)
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
    // The breathing slots stay fixed per lyrics — every window the native
    // gate allows, keyed by the item whose line it leads in front of. The
    // slot canvas itself reads playback, so the dots behave exactly like
    // the native view: frozen while paused, collapsed outside the window.
    val breathingSlots = remember(lyrics) { FallbackKaraokeModel.breathingSlots(lyrics) }

    // The root carries the artwork-overlay request and clips its children:
    // the scrolling rows and their breathing slots share one blended
    // layer, so Plus / Screen glow like the native view applies to both.
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
                // The slot rides above its line, inside the same >5 s windows
                // the native view breathes in — the line beneath rises into
                // the anchor once the singing starts.
                breathingSlots[index]?.let { slot ->
                    BreathingDots(
                        slot = slot,
                        alignedEnd = line is KaraokeLine && line.alignment == KaraokeAlignment.End,
                        color = colors.text,
                        slotStyle = normalLineTextStyle,
                        position = { position.value },
                    )
                }
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
                    // Standalone accompaniment rows honor their own line's
                    // alignment; the nested pair must too, since its value
                    // may differ from the parent main line's.
                    val nestedAlign =
                        if (accompaniment.alignment == KaraokeAlignment.End) {
                            TextAlign.End
                        } else {
                            TextAlign.Start
                        }
                    if (active) {
                        KaraokeProgressText(
                            syllables = accompaniment.syllables,
                            baseStyle = accompanimentLineTextStyle,
                            baseColor = colors.text.copy(
                                alpha = colors.text.alpha * AccompanimentRowAlpha,
                            ),
                            textAlign = nestedAlign,
                            position = position,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AccompanimentIndent),
                        )
                    } else {
                        RuiText(
                            text = accompaniment.syllables.joinToString("") { it.content },
                            style = accompanimentLineTextStyle,
                            color = colors.text.copy(
                                alpha = colors.text.alpha * AccompanimentRowAlpha * UnfocusedRowAlpha,
                            ),
                            textAlign = nestedAlign,
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
                        baseColor = colors.text.copy(
                            alpha = colors.text.alpha * AccompanimentRowAlpha,
                        ),
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
                        color = colors.text.copy(
                            alpha = colors.text.alpha * AccompanimentRowAlpha * UnfocusedRowAlpha,
                        ),
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
                        color = colors.text.copy(
                            alpha = colors.text.alpha * if (active) 1f else UnfocusedRowAlpha,
                        ),
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
            color = colors.text.copy(alpha = colors.text.alpha * UnfocusedRowAlpha),
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
        color = color.copy(
            alpha = color.alpha * CaptionRowAlpha * if (active) 1f else UnfocusedRowAlpha,
        ),
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * The native view's breathing slot collapsed into the fallback renderer:
 * one canvas whose height, dot scale and alpha all follow the playback
 * envelope, mirroring KaraokeBreathingDots — the dots sit [BreathingDotNumber]
 * abreast, [BreathingDotsIndent] inside the row, centered in a
 * main-vocal-line-high slot whose trailing spacing collapses with it, so the
 * line beneath rises into the anchor without a residual gap.
 */
@Composable
private fun BreathingDots(
    slot: BreathingSlot,
    alignedEnd: Boolean,
    color: Color,
    slotStyle: TextStyle,
    position: () -> Int,
) {
    val envelope = remember(slot) { BreathingEnvelope(slot.startMs, slot.endMs) }
    val time by rememberUpdatedState(position)
    val visibility = remember(envelope) {
        derivedStateOf { envelope.visibility(envelope.elapsed(time())) }
    }
    val slotHeight = with(LocalDensity.current) {
        // One main-vocal em box, per the native view's breathing line height
        // (the tallest main-vocal row), so the slot rhythm reads like a line.
        val lineHeight = slotStyle.lineHeight.takeIf { !it.isUnspecified && it.isSp }
        val fontSize = slotStyle.fontSize.takeIf { !it.isUnspecified && it.isSp }
        ((lineHeight ?: fontSize ?: BreathingSlotFallbackFontSize) * BreathingSlotFontScale)
            .toDp()
    }.coerceAtLeast(BreathingDotSize)
    Canvas(
        Modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                // The slot's height follows playback: invisible outside the
                // window, one full line slot inside it — the native view's
                // collapsing row, so the entrance and exit never pop.
                layout(placeable.width, (placeable.height * visibility.value).roundToInt()) {
                    placeable.place(0, 0)
                }
            }
            .padding(horizontal = BreathingDotsIndent)
            // Trailing spacing collapses with the slot, so hidden dots leave
            // no residual list gap.
            .padding(bottom = FallbackItemSpacing)
            .height(slotHeight),
    ) {
        val now = envelope.elapsed(time())
        val scale = envelope.scale(now)
        val alpha = envelope.alpha(now)
        if (scale > 0f && alpha > 0f) {
            val diameter = BreathingDotSize.toPx().coerceAtLeast(1f)
            val margin = BreathingDotSpacing.toPx().coerceAtLeast(0f)
            val totalWidth = diameter * BreathingDotNumber + margin * (BreathingDotNumber - 1)
            val origin = if (alignedEnd) size.width - totalWidth else 0f
            val center = origin + totalWidth / 2f
            val radius = diameter / 2f * scale
            repeat(BreathingDotNumber) { index ->
                val baseX = origin + diameter / 2f + (diameter + margin) * index
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(
                        x = center + (baseX - center) * scale,
                        y = size.height / 2f,
                    ),
                    alpha = alpha * envelope.dotAlpha(index, now),
                )
            }
        }
    }
}
