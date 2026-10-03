package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import rui.foundation.blurSurface
import rui.foundation.BlurBackdrop
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.foundation.platform.isBlurSupported
import rui.theme.RuiTheme

/**
 * The app bar: a small centered-title bar, or a large-title variant for entry
 * screens. With a [backdrop] the bar renders as frosted glass over scrolling
 * content; otherwise it is a plain surface. Always closes with a hairline.
 *
 * Place inside [RuiScaffold]'s `topBar` slot; content that scrolls under the
 * bar is what makes the glass readable.
 */
@Composable
public fun RuiTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    largeTitle: Boolean = false,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    backdrop: BlurBackdrop? = null,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    val barBackground = if (backdrop != null && isBlurSupported) {
        Modifier.blurSurface(
            backdrop = backdrop,
            radius = 32.dp,
            tint = colors.surface.copy(alpha = 0.65f),
        )
    } else {
        Modifier.background(colors.surface)
    }

    Column(modifier = modifier.fillMaxWidth().then(barBackground).statusBarsPadding()) {
        if (largeTitle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .padding(horizontal = spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBack) {
                    RuiIconButton(
                        onClick = onBack,
                        variant = RuiButtonVariant.Plain,
                        modifier = Modifier.padding(end = spacing.xs),
                    ) {
                        RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = "Back")
                    }
                }
                RuiText(
                    text = title,
                    style = typography.titleLg,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                actions()
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showBack) {
                        RuiIconButton(onClick = onBack, variant = RuiButtonVariant.Plain) {
                            RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = "Back")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    actions()
                }
                RuiText(
                    text = title,
                    style = typography.titleMd,
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = 1,
                )
            }
        }
        RuiDivider()
    }
}
