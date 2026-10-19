package rui.components

import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeAlignment
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeSyllable
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The fallback lyric renderer's decisions: the focus window it derives from
 * the playlist timings and the syllable fill ramp. Compose choreography
 * needs a real frame loop — these cover the logic around it.
 */
class RuiKaraokeLyricsFallbackTest {

    private fun syllable(content: String, start: Int, end: Int) =
        KaraokeSyllable(content, start, end)

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

    @Test
    fun activeWindowCoversSimultaneousRows() {
        assertContentEquals(intArrayOf(1, 2), FallbackKaraokeModel.activeIndices(duet, 600))
        assertContentEquals(intArrayOf(), FallbackKaraokeModel.activeIndices(duet, 950))
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
    fun emptyLyricsHaveNoTarget() {
        val empty = SyncedLyrics(lines = emptyList())
        assertEquals(-1, FallbackKaraokeModel.targetIndex(empty, 100))
    }

    @Test
    fun breathingWindowCoversIntroAndInterludeOnly() {
        // Intro before the first line and the interlude gap breathe; a
        // playing span and the quiet tail after the song do not.
        assertTrue(FallbackKaraokeModel.isIntroOrInterlude(duet, -50))
        assertTrue(FallbackKaraokeModel.isIntroOrInterlude(duet, 350))
        assertFalse(FallbackKaraokeModel.isIntroOrInterlude(duet, 250))
        assertFalse(FallbackKaraokeModel.isIntroOrInterlude(duet, 450))
        assertFalse(FallbackKaraokeModel.isIntroOrInterlude(duet, 950))
        val empty = SyncedLyrics(lines = emptyList())
        assertFalse(FallbackKaraokeModel.isIntroOrInterlude(empty, 100))
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
