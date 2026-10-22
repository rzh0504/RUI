package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.UnstyledAvatar
import rui.foundation.RuiText
import rui.theme.RuiTheme

/** Visual weight of a [RuiAvatar]. */
public enum class RuiAvatarSize { Sm, Md, Lg }

/**
 * Colors of [RuiAvatar]'s fallback (initials and placeholder). Defaults are
 * deliberately muted — a quiet placeholder, not an identity color; pass a
 * branded pair to change that.
 */
@Immutable
public data class RuiAvatarColors(
    public val container: Color,
    public val content: Color,
)

public object RuiAvatarDefaults {

    public fun diameter(size: RuiAvatarSize): Dp = when (size) {
        RuiAvatarSize.Sm -> 28.dp
        RuiAvatarSize.Md -> 40.dp
        RuiAvatarSize.Lg -> 56.dp
    }

    @Composable
    public fun colors(
        container: Color = RuiTheme.colors.surfaceMuted,
        content: Color = RuiTheme.colors.contentMuted,
    ): RuiAvatarColors = RuiAvatarColors(
        container = container,
        content = content,
    )
}

/**
 * A circular avatar: an optional image over a fallback underlay of [initials]
 * (or a quiet placeholder when both are absent). Behavior and semantics come
 * from Unstyled's [UnstyledAvatar]; the circle, the size tiers and the muted
 * fallback colors come from Rui's tokens.
 *
 * Avatars use pure `CircleShape` — like the switch, exempt from corner
 * smoothing: circles read as circles.
 *
 * @param painter Optional image drawn over the fallback; `null` shows the
 *   underlay alone.
 * @param initials Fallback text (typically 1–2 characters), centered in the
 *   circle. Ignored when empty.
 * @param size One of the three diameter tiers, see [RuiAvatarDefaults.diameter].
 * @param contentScale How [painter] scales inside the circle.
 * @param colors Fallback colors, see [RuiAvatarDefaults.colors].
 * @param contentDescription Accessibility text describing what the avatar
 *   represents.
 */
@Composable
public fun RuiAvatar(
    painter: Painter? = null,
    modifier: Modifier = Modifier,
    initials: String? = null,
    size: RuiAvatarSize = RuiAvatarSize.Md,
    contentScale: ContentScale = ContentScale.Crop,
    colors: RuiAvatarColors = RuiAvatarDefaults.colors(),
    contentDescription: String? = null,
) {
    UnstyledAvatar(
        painter = painter,
        modifier = modifier
            .size(RuiAvatarDefaults.diameter(size))
            .clip(CircleShape),
        contentDescription = contentDescription,
        contentScale = contentScale,
        underlay = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.container),
                contentAlignment = Alignment.Center,
            ) {
                if (!initials.isNullOrEmpty()) {
                    RuiText(
                        text = initials,
                        style = when (size) {
                            RuiAvatarSize.Sm -> RuiTheme.typography.labelLg
                            RuiAvatarSize.Md -> RuiTheme.typography.titleMd
                            RuiAvatarSize.Lg -> RuiTheme.typography.titleLg
                        },
                        color = colors.content,
                    )
                }
            }
        },
    )
}
