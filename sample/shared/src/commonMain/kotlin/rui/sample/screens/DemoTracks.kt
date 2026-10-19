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
 * interjections (the second one syllable-timed). Times are deliberately gapped
 * so breathing dots and the follow-vs-pause logic both appear.
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
                        phrase("霓", 5_000, 5_500),
                        phrase("虹", 5_500, 6_000),
                        phrase("把", 6_000, 6_400),
                        phrase("夜色", 6_400, 7_200),
                        phrase("调成蓝", 7_200, 8_600),
                    ),
                    translation = "Neons tune the night to blue",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 5_000,
                    end = 8_600,
                    phonetic = "ní hóng bǎ yè sè tiáo chéng lán",
                ),
                KaraokeLine.AccompanimentKaraokeLine(
                    syllables = listOf(phrase("ooh", 9_400, 10_200), phrase("ooh", 10_400, 11_400)),
                    translation = null,
                    alignment = KaraokeAlignment.Unspecified,
                    start = 9_400,
                    end = 11_400,
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("电话亭里", 12_500, 13_600),
                        phrase("住着", 13_600, 14_200),
                        phrase("一场", 14_200, 14_800),
                        phrase("未完的雨", 14_800, 16_500),
                    ),
                    translation = "A phone booth shelters an unfinished rain",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 12_500,
                    end = 16_500,
                ),
                SyncedLine(
                    content = "车灯扫过斑马线像琴键",
                    translation = "Headlights sweep the crosswalk like keys",
                    start = 18_000,
                    end = 21_500,
                ),
                KaraokeLine.AccompanimentKaraokeLine(
                    syllables = listOf(phrase("走吧", 22_500, 23_400), phrase("走吧", 23_600, 25_000)),
                    translation = null,
                    alignment = KaraokeAlignment.End,
                    start = 22_500,
                    end = 25_000,
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("我们", 26_500, 27_100),
                        phrase("把心事", 27_100, 27_900),
                        phrase("寄给", 27_900, 28_400),
                        phrase("高架的风", 28_400, 30_500),
                    ),
                    translation = "We mail our worries to the highway wind",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 26_500,
                    end = 30_500,
                    phonetic = "wǒ men bǎ xīn shì jì gěi gāo jià de fēng",
                ),
                SyncedLine(
                    content = "晚报说明天依然有光",
                    translation = "The evening paper says tomorrow keeps its light",
                    start = 32_000,
                    end = 35_500,
                    phonetic = "wǎn bào shuō míng tiān yī rán yǒu guāng",
                ),
                KaraokeLine.MainKaraokeLine(
                    syllables = listOf(
                        phrase("那就", 37_000, 37_600),
                        phrase("让音符", 37_600, 38_400),
                        phrase("替我们", 38_400, 39_200),
                        phrase("熬夜", 39_200, 41_500),
                    ),
                    translation = "So let the notes stay up for us",
                    alignment = KaraokeAlignment.Unspecified,
                    start = 37_000,
                    end = 41_500,
                ),
            ),
        ),
    ),
)
