package rui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.TextStyle
import com.mocharealm.accompanist.lyrics.core.model.ISyncedLine
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics

/**
 * The browser target: Accompanist's `lyrics-ui` artifact has no wasm-js
 * variant, so the wrapper binds its pure-Compose fallback renderer with the
 * blur focus fully disabled (alpha dimming still marks the focus).
 */
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
    FallbackKaraokeLyricsView(
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
        modifier = modifier,
    )
}
