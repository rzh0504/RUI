package rui.foundation

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.composeunstyled.LocalContentColor
import com.composeunstyled.UnstyledIcon
import rui.theme.RuiTheme

/**
 * Icon with the design system's default size. The tint defaults to the ambient
 * content color, so icons inside buttons and surfaces pick up the right color
 * automatically. Pass [contentDescription] for meaningful icons; `null` marks a
 * decorative one.
 */
@Composable
public fun RuiIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = RuiTheme.dimens.iconSize,
    tint: Color = Color.Unspecified,
) {
    val resolvedTint = if (tint.isUnspecified) LocalContentColor.current else tint

    UnstyledIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.then(if (size != Dp.Unspecified) Modifier.size(size) else Modifier),
        tint = resolvedTint,
    )
}

@Composable
public fun RuiIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = RuiTheme.dimens.iconSize,
    tint: Color = Color.Unspecified,
) {
    val resolvedTint = if (tint.isUnspecified) LocalContentColor.current else tint

    UnstyledIcon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.then(if (size != Dp.Unspecified) Modifier.size(size) else Modifier),
        tint = resolvedTint,
    )
}
