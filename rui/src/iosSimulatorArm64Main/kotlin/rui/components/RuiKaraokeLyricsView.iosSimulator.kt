package rui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.TextStyle
import com.mocharealm.accompanist.lyrics.core.model.ISyncedLine
import com.mocharealm.accompanist.lyrics.core.model.SyncedLyrics

/**
 * The iOS simulator target: Accompanist's `lyrics-ui` artifact publishes
 * only an iosArm64 variant, so simulator builds bind the pure-Compose
 * fallback; device builds above use the native renderer.
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
