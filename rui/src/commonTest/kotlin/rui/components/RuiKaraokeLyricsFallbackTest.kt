package rui.components

import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeAlignment
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeSyllable
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The fallback lyric renderer's decisions: the focus window it derives from
 * the playlist timings, the breathing-dot gates (the native timeline's
 * strict five-second rule) and the syllable fill ramp. Compose choreography
 * needs a real frame loop — these cover the logic around it.
 */
class RuiKaraokeLyricsFallbackTest {

    private fun syllable(content: String, start: Int, end: Int) =
        KaraokeSyllable(content, start, end)

    private fun mainLine(content: String, start: Int, end: Int) =
        KaraokeLine.MainKaraokeLine(
            syllables = listOf(syllable(content, start, end)),
            translation = null,
            alignment = KaraokeAlignment.Unspecified,
            start = start,
            end = end,
        )

    /** verse (0-300), harmony (400-700), chorus (500-900) — with a 300..400 gap. */
    private val duet = SyncedLyrics(
        lines = listOf(
            KaraokeLine.MainKaraokeLine(
                syllables = listOf(syllable("he", 0, 150), syllable("llo", 150, 300)),
                translation = "你好",
                alignment = KaraokeAlignment.Unspecified,
                start = 0,
                end = 300,
            ),
            KaraokeLine.AccompanimentKaraokeLine(
                syllables = listOf(syllable("ah", 400, 700)),
                translation = null,
                alignment = KaraokeAlignment.End,
                start = 400,
                end = 700,
            ),
            KaraokeLine.MainKaraokeLine(
                syllables = listOf(syllable("bye", 500, 900)),
                translation = null,
                alignment = KaraokeAlignment.Unspecified,
                start = 500,
                end = 900,
            ),
        ),
    )

    /** An eight-second intro: wide enough for the native breathing gate. */
    private val opening = SyncedLyrics(
        lines = listOf(mainLine("he", 8_000, 8_300)),
    )

    /** An intro parked exactly on the strict gate — still quiet. */
    private val quiet = SyncedLyrics(
        lines = listOf(
            mainLine("he", 5_000, 5_300),
            mainLine("bye", 5_400, 5_800),
        ),
    )

    /**
     * [0..1_000] — [7_500..10_000] — [17_000..17_500] — [18_000..18_500]:
     * a six-and-a-half-second interlude slot above line 1, a seven-second
     * slot above line 2, plus short gaps that stay quiet.
     */
    private val gapped = SyncedLyrics(
        lines = listOf(
            mainLine("one", 0, 1_000),
            mainLine("two", 7_500, 10_000),
            mainLine("three", 17_000, 17_500),
            mainLine("four", 18_000, 18_500),
        ),
    )

    @Test
    fun activeWindowCoversSimultaneousRows() {
        assertContentEquals(intArrayOf(1, 2), FallbackKaraokeModel.activeIndices(duet, 600))
        assertContentEquals(intArrayOf(), FallbackKaraokeModel.activeIndices(duet, 950))
    }

    @Test
    fun activeWindowIncludesNestedAccompanimentBounds() {
        val nested = SyncedLyrics(
            lines = listOf(
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(syllable("lead", 10_000, 12_000)),
                    translation = null,
                    alignment = KaraokeAlignment.Unspecified,
                    start = 10_000,
                    end = 12_000,
                    accompanimentLines = listOf(
                        KaraokeLine.AccompanimentKaraokeLine(
                            syllables = listOf(syllable("before", 9_000, 10_000)),
                            translation = null,
                            alignment = KaraokeAlignment.Start,
                            start = 9_000,
                            end = 10_000,
                        ),
                        KaraokeLine.AccompanimentKaraokeLine(
                            syllables = listOf(syllable("after", 12_000, 15_000)),
                            translation = null,
                            alignment = KaraokeAlignment.End,
                            start = 12_000,
                            end = 15_000,
                        ),
                    ),
                ),
                mainLine("next", 20_000, 21_000),
            ),
        )

        assertContentEquals(intArrayOf(0), FallbackKaraokeModel.activeIndices(nested, 9_500))
        assertContentEquals(intArrayOf(0), FallbackKaraokeModel.activeIndices(nested, 11_000))
        assertContentEquals(intArrayOf(0), FallbackKaraokeModel.activeIndices(nested, 13_500))
        assertContentEquals(intArrayOf(), FallbackKaraokeModel.activeIndices(nested, 16_000))
        assertEquals(0, FallbackKaraokeModel.targetIndex(nested, 13_500))
    }

    @Test
    fun targetFollowsFirstActiveLine() {
        assertEquals(0, FallbackKaraokeModel.targetIndex(duet, 250))
        assertEquals(1, FallbackKaraokeModel.targetIndex(duet, 600))
        assertEquals(2, FallbackKaraokeModel.targetIndex(duet, 800))
    }

    @Test
    fun targetParksEndsOfTheSong() {
        // A leading intro anchors the head; silence past the song anchors
        // the tail; the gap guard keeps interlude gaps at the current line.
        assertEquals(0, FallbackKaraokeModel.targetIndex(duet, -50))
        assertEquals(duet.lines.lastIndex, FallbackKaraokeModel.targetIndex(duet, 60_000))
        assertEquals(-1, FallbackKaraokeModel.targetIndex(duet, 350))
    }

    @Test
    fun targetParksAheadOfBreathingInterludes() {
        // Wide interlude: the follow parks on the upcoming line, exactly
        // where its dots breathe above it; short gaps keep the current line.
        assertEquals(1, FallbackKaraokeModel.targetIndex(gapped, 4_000))
        assertEquals(2, FallbackKaraokeModel.targetIndex(gapped, 12_000))
        assertEquals(-1, FallbackKaraokeModel.targetIndex(gapped, 17_700))
        // The intro parks the head item until the first line sings.
        assertEquals(0, FallbackKaraokeModel.targetIndex(opening, 4_000))
    }

    @Test
    fun breathingNeedsAWideIntroBeforeTheFirstLine() {
        // The gate mirrors the native timeline: a strict five-second wait
        // before the dots appear, and the quiet tail before zero never
        // breathes.
        assertTrue(FallbackKaraokeModel.isIntroBreathing(opening, 0))
        assertTrue(FallbackKaraokeModel.isIntroBreathing(opening, 7_999))
        assertEquals(false, FallbackKaraokeModel.isIntroBreathing(opening, 8_000))
        assertEquals(false, FallbackKaraokeModel.isIntroBreathing(opening, -1))
        // A five-second wait sits exactly on the strict gate — still quiet.
        assertEquals(false, FallbackKaraokeModel.isIntroBreathing(quiet, 2_000))
        // No lyrics, nothing breathes.
        val empty = SyncedLyrics(lines = emptyList())
        assertEquals(false, FallbackKaraokeModel.isIntroBreathing(empty, 100))
    }

    @Test
    fun interludeBreathesOnlyOnWideGapsAboveTheUpcomingLine() {
        // The window runs from the instant the previous line ends to the
        // instant the waiting line starts.
        assertEquals(1, FallbackKaraokeModel.activeInterludeIndex(gapped, 1_000))
        assertEquals(1, FallbackKaraokeModel.activeInterludeIndex(gapped, 7_499))
        assertEquals(2, FallbackKaraokeModel.activeInterludeIndex(gapped, 10_000))
        assertEquals(2, FallbackKaraokeModel.activeInterludeIndex(gapped, 16_999))
        assertEquals(-1, FallbackKaraokeModel.activeInterludeIndex(gapped, 17_000))
        assertEquals(-1, FallbackKaraokeModel.activeInterludeIndex(gapped, -1))
        assertEquals(-1, FallbackKaraokeModel.activeInterludeIndex(gapped, 7_600))
        // Short gaps and overlapping duet rows stay quiet; a single line
        // has no interlude to breathe.
        assertEquals(-1, FallbackKaraokeModel.activeInterludeIndex(duet, 350))
        val single = SyncedLyrics(lines = listOf(mainLine("solo", 0, 300)))
        assertEquals(-1, FallbackKaraokeModel.activeInterludeIndex(single, 150))
    }

    @Test
    fun breathingSlotsMapTheNativeGate() {
        // The intro slot leads item 0; the wide interlude slots lead the
        // lines they wait on — the exact windows the native gate allows.
        assertEquals(
            mapOf(
                1 to BreathingSlot(1_000, 7_500),
                2 to BreathingSlot(10_000, 17_000),
            ),
            FallbackKaraokeModel.breathingSlots(gapped),
        )
        // The wide intro breathes at the head.
        assertEquals(
            mapOf(0 to BreathingSlot(0, 8_000)),
            FallbackKaraokeModel.breathingSlots(opening),
        )
        // Narrow gaps — or waits parked exactly on the gate — slip through
        // silent, on both engines.
        assertEquals(emptyMap(), FallbackKaraokeModel.breathingSlots(quiet))
        assertEquals(emptyMap(), FallbackKaraokeModel.breathingSlots(duet))
        assertEquals(emptyMap(), FallbackKaraokeModel.breathingSlots(SyncedLyrics(lines = emptyList())))
    }

    @Test
    fun breathingEnvelopeDressesTheWindow() {
        val envelope = BreathingEnvelope(0, 8_000)
        // The slot is invisible outside the window and fully open inside.
        assertEquals(0f, envelope.visibility(-10f))
        assertEquals(0f, envelope.visibility(8_000f))
        assertEquals(1f, envelope.visibility(400f))
        assertTrue(
            envelope.visibility(110f) in 0f..1f && envelope.visibility(110f) < 1f,
            "the entrance ramp should build up, ${envelope.visibility(110f)}",
        )
        assertTrue(
            envelope.visibility(7_890f) in 0f..1f && envelope.visibility(7_890f) < 1f,
            "the exit ramp should settle down, ${envelope.visibility(7_890f)}",
        )
        // Dots fade in over the enter ramp, then hold full alpha while
        // they breathe, and ride the scale wave at a fraction of one.
        assertEquals(0f, envelope.scale(-1f))
        assertTrue(
            envelope.alpha(1_000f) in 0f..1f && envelope.alpha(1_000f) < 1f,
            "alpha ramps in during the enter phase, ${envelope.alpha(1_000f)}",
        )
        assertEquals(0.5f, envelope.alpha(1_500f))
        assertTrue(envelope.scale(1_500f) > 0f && envelope.scale(1_500f) <= 1f)
        assertEquals(1f, envelope.alpha(5_000f))
        assertTrue(envelope.scale(5_000f) > 0f && envelope.scale(5_000f) <= 1f)
        // The brightness stagger walks across the row as it enters.
        assertEquals(0.6f, envelope.dotAlpha(0, 3_800f), 0.001f)
        assertEquals(0.2f, envelope.dotAlpha(2, 3_800f), 0.001f)
        // A window far shorter than the phase set still stays inside it;
        // the envelope reads elapsed time, so 600 ms in is 600 ms elapsed.
        val squeezed = BreathingEnvelope(4_000, 5_200)
        assertTrue(
            squeezed.scale(600f).let { it > 0f && it <= 1f },
            "squeezed scale at elapsed 600, ${squeezed.scale(600f)}",
        )
        assertEquals(1f, squeezed.visibility(600f))
        assertEquals(0f, squeezed.visibility(-1f))
        assertEquals(0f, squeezed.visibility(1_200f))
    }

    @Test
    fun emptyLyricsHaveNoTarget() {
        val empty = SyncedLyrics(lines = emptyList())
        assertEquals(-1, FallbackKaraokeModel.targetIndex(empty, 100))
    }

    @Test
    fun highlightAlphaRunsTheRowContrast() {
        assertEquals(UnfocusedRowAlpha, FallbackKaraokeModel.syllableHighlightAlpha(0f), 0.001f)
        assertEquals(UnfocusedRowAlpha, FallbackKaraokeModel.syllableHighlightAlpha(-1f), 0.001f)
        assertEquals(1f, FallbackKaraokeModel.syllableHighlightAlpha(1f), 0.001f)
        assertEquals(1f, FallbackKaraokeModel.syllableHighlightAlpha(5f), 0.001f)
        val mid = FallbackKaraokeModel.syllableHighlightAlpha(0.5f)
        assertTrue(
            mid > UnfocusedRowAlpha && mid < 1f,
            "mid-point alpha $mid should sit between the extremes",
        )
    }
}
