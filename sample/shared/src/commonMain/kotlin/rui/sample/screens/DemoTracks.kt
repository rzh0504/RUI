package rui.sample.screens

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeAlignment
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeLine
import com.mocharealm.accompanist.lyrics.core.model.karaoke.KaraokeSyllable
import com.mocharealm.accompanist.lyrics.core.model.synced.SyncedLine

internal fun phrase(text: String, start: Int, end: Int) = KaraokeSyllable(text, start, end)

/** `m:ss` readout of a position in milliseconds, manually padded for KMP. */
internal fun clockLabel(ms: Int): String {
    val totalSeconds = ms / 1_000
    val minutes = totalSeconds / 60
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

/** One demo song: mock artwork palette plus the synced lyrics rendered on it. */
internal data class DemoTrack(
    val title: String,
    val artist: String,
    val durationMs: Int,
    /** Thumb tile gradient, sized to 48dp in the player header. */
    val artwork: Brush,
    /** Page wash behind the lyrics: dark, artwork-derived. */
    val background: Brush,
    val lyrics: SyncedLyrics,
)

/**
 * The two demo tracks the player blocks rotate through. Both mix the lyrics
 * vocabulary: phonetic + translation main lines, a main line carrying a nested
 * accompaniment hook, plain synced rows and right-aligned accompaniment
 * interjections (the second one syllable-timed). The eight-second intro clears
 * the native five-second breathing gate so the dots appear on every platform,
 * while the in-song gaps stay narrow so the follow-vs-pause logic appears
 * without flickering dots.
 */
internal val DemoTracks: List<DemoTrack> = listOf(
    DemoTrack(
        title = "Dawn by the window",
        artist = "Rui demo · Morning tape",
        durationMs = 50_000,
        artwork = Brush.verticalGradient(
            listOf(Color(0xFF8EA0D8), Color(0xFF3C4864)),
        ),
        background = Brush.verticalGradient(
            listOf(Color(0xFF101018), Color(0xFF1E1E2C)),
        ),
        lyrics = SyncedLyrics(
            id = "demo-dawn",
            lines = listOf(
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("晨", 8_000, 8_600),
                        phrase("光", 8_600, 9_100),
                        phrase("落", 9_100, 9_500),
                        phrase("在", 9_500, 9_800),
                        phrase("窗台", 9_800, 10_300),
                    ),
                    translation = "Morning light spills on the windowsill",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 8_000,
                    end = 10_300,
                    phonetic = "chén guāng luò zài chuāng tái",
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("揉", 11_000, 11_500),
                        phrase("醒", 11_500, 12_000),
                        phrase("沉", 12_000, 12_600),
                        phrase("睡", 12_600, 13_100),
                        phrase("的", 13_100, 13_400),
                        phrase("台钟", 13_400, 13_800),
                    ),
                    translation = "Winding up the sleeping clock",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 11_000,
                    end = 13_800,
                    accompanimentLines = listOf(
                        KaraokeLine.AccompanimentKaraokeLine(
                            syllables = listOf(phrase("yeah", 11_600, 12_300), phrase("yeah", 12_600, 13_600)),
                            translation = null,
                            alignment = KaraokeAlignment.Unspecified,
                            start = 11_600,
                            end = 13_600,
                        ),
                    ),
                ),
                SyncedLine(
                    content = "旧街的梧桐还没记住这阵风",
                    translation = "The old street's poplar hasn't caught the wind",
                    start = 14_400,
                    end = 17_400,
                ),
                KaraokeLine.AccompanimentKaraokeLine(
                    syllables = listOf(phrase("哦", 18_000, 19_700), phrase("哦——", 19_700, 21_000)),
                    translation = null,
                    alignment = KaraokeAlignment.End,
                    start = 18_000,
                    end = 21_000,
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("把", 22_500, 23_000),
                        phrase("黄", 23_000, 23_500),
                        phrase("昏", 23_500, 24_000),
                        phrase("拧", 24_000, 24_500),
                        phrase("成", 24_500, 24_800),
                        phrase("发条", 24_800, 25_300),
                    ),
                    translation = null,
                    alignment = KaraokeAlignment.Unspecified,
                    start = 22_500,
                    end = 25_300,
                    phonetic = "bǎ huáng hūn nǐng chéng fā tiáo",
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(phrase("等", 26_000, 26_800), phrase("月亮过河", 26_800, 29_200)),
                    translation = "Waiting for the moon to cross the river",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 26_000,
                    end = 29_200,
                ),
                SyncedLine(
                    content = "有一段旋律还没有名字",
                    translation = "There is a melody still unnamed",
                    start = 30_500,
                    end = 34_500,
                    phonetic = "yǒu yī duàn xuán lǜ hái méi yǒu míng zi",
                ),
                KaraokeLine.AccompanimentKaraokeLine(
                    syllables = listOf(
                        phrase("la", 36_000, 37_100),
                        phrase("la", 37_100, 38_200),
                        phrase("la", 38_200, 39_500),
                    ),
                    translation = null,
                    alignment = KaraokeAlignment.End,
                    start = 36_000,
                    end = 39_500,
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(phrase("名字", 41_000, 41_900), phrase("写进", 41_900, 42_500), phrase("第二段", 42_500, 44_200)),
                    translation = "The name finds the second verse",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 41_000,
                    end = 44_200,
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(phrase("而风", 45_500, 46_500), phrase("还在", 46_500, 47_200), phrase("写落款", 47_200, 48_500)),
                    translation = "While the wind keeps writing the signature",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 45_500,
                    end = 48_500,
                ),
            ),
        ),
    ),
    DemoTrack(
        title = "Neon overpass",
        artist = "Rui demo · Night drive",
        durationMs = 46_000,
        artwork = Brush.verticalGradient(
            listOf(Color(0xFFC86BD8), Color(0xFF4A2680)),
        ),
        background = Brush.verticalGradient(
            listOf(Color(0xFF241033), Color(0xFF0D0919)),
        ),
        lyrics = SyncedLyrics(
            id = "demo-neon",
            lines = listOf(
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("霓", 8_000, 8_500),
                        phrase("虹", 8_500, 9_000),
                        phrase("把", 9_000, 9_400),
                        phrase("夜色", 9_400, 10_200),
                        phrase("调成蓝", 10_200, 11_600),
                    ),
                    translation = "Neons tune the night to blue",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 8_000,
                    end = 11_600,
                    phonetic = "ní hóng bǎ yè sè tiáo chéng lán",
                ),
                KaraokeLine.AccompanimentKaraokeLine(
                    syllables = listOf(phrase("ooh", 12_400, 13_200), phrase("ooh", 13_400, 14_400)),
                    translation = null,
                    alignment = KaraokeAlignment.Unspecified,
                    start = 12_400,
                    end = 14_400,
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("电话亭里", 15_500, 16_600),
                        phrase("住着", 16_600, 17_200),
                        phrase("一场", 17_200, 17_800),
                        phrase("未完的雨", 17_800, 19_500),
                    ),
                    translation = "A phone booth shelters an unfinished rain",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 15_500,
                    end = 19_500,
                ),
                SyncedLine(
                    content = "车灯扫过斑马线像琴键",
                    translation = "Headlights sweep the crosswalk like keys",
                    start = 21_000,
                    end = 24_500,
                ),
                KaraokeLine.AccompanimentKaraokeLine(
                    syllables = listOf(phrase("走吧", 25_500, 26_400), phrase("走吧", 26_600, 28_000)),
                    translation = null,
                    alignment = KaraokeAlignment.End,
                    start = 25_500,
                    end = 28_000,
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("我们", 29_500, 30_100),
                        phrase("把心事", 30_100, 30_900),
                        phrase("寄给", 30_900, 31_400),
                        phrase("高架的风", 31_400, 33_500),
                    ),
                    translation = "We mail our worries to the highway wind",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 29_500,
                    end = 33_500,
                    phonetic = "wǒ men bǎ xīn shì jì gěi gāo jià de fēng",
                ),
                SyncedLine(
                    content = "晚报说明天依然有光",
                    translation = "The evening paper says tomorrow keeps its light",
                    start = 35_000,
                    end = 38_500,
                    phonetic = "wǎn bào shuō míng tiān yī rán yǒu guāng",
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("那就", 40_000, 40_600),
                        phrase("让音符", 40_600, 41_400),
                        phrase("替我们", 41_400, 42_200),
                        phrase("熬夜", 42_200, 44_500),
                    ),
                    translation = "So let the notes stay up for us",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 40_000,
                    end = 44_500,
                ),
            ),
        ),
    ),
)
