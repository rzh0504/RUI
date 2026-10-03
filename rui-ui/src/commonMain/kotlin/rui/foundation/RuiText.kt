package rui.foundation

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.composeunstyled.Text
import rui.theme.RuiTheme

/**
 * Text with the design system's defaults: [rui.theme.RuiTypography.bodyMd] and
 * the ambient content color. All parameters are overridable for one-off cases.
 */
@Composable
public fun RuiText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = RuiTheme.typography.bodyMd,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign ?: TextAlign.Unspecified,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout,
    )
}
