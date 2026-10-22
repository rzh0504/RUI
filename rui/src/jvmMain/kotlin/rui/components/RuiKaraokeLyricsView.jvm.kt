package rui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mocharealm.accompanist.lyrics.core.model.ISyncedLine
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics
import com.mocharealm.accompanist.lyrics.ui.composable.list.LyricsLazyListState
import com.mocharealm.accompanist.lyrics.ui.composable.lyrics.KaraokeBreathingDotsDefaults
import com.mocharealm.accompanist.lyrics.ui.composable.lyrics.KaraokeLyricsView
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.TextStyle

@Composable
internal actual fun PlatformKaraokeLyricsView(
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
) {
    val listState = remember { LyricsLazyListState() }
    KaraokeLyricsView(
        listState = listState,
        lyrics = lyrics,
        currentPosition = currentPosition,
        onLineClicked = onLineClicked,
        onLinePressed = onLinePressed,
        modifier = modifier,
        normalLineTextStyle = normalLineTextStyle,
        translationTextStyle = translationTextStyle,
        accompanimentLineTextStyle = accompanimentLineTextStyle,
        textColor = colors.text,
        breathingDotsDefaults = KaraokeBreathingDotsDefaults(breathingDotsColor = colors.text),
        phoneticTextStyle = phoneticTextStyle,
        blendMode = blendMode,
        useBlurEffect = useBlurEffect,
        showTranslation = showTranslation,
        showPhonetic = showPhonetic,
    )
}
