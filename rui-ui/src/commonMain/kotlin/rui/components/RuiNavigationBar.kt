package rui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import rui.foundation.blurSurface
import rui.foundation.BlurBackdrop
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.foundation.platform.isBlurSupported
import rui.theme.RuiTheme

/** One destination of [RuiNavigationBar]. */
public data class RuiNavigationBarItem(
    public val label: String,
    public val icon: ImageVector? = null,
)

/**
 * The bottom navigation bar. With a [backdrop] the bar renders as frosted
 * glass over content that scrolls beneath it; otherwise a plain surface.
 * Place inside [RuiScaffold]'s `bottomBar` slot.
 */
@Composable
public fun RuiNavigationBar(
    items: List<RuiNavigationBarItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BlurBackdrop? = null,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography

    val barBackground = if (backdrop != null && isBlurSupported) {
        Modifier.blurSurface(
            backdrop = backdrop,
            radius = 32.dp,
            tint = colors.surface.copy(alpha = 0.65f),
        )
    } else {
        Modifier.background(colors.surface)
    }

    Column(modifier = modifier.fillMaxWidth().then(barBackground)) {
        RuiDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .clickable(role = Role.Tab) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (item.icon != null) {
                            RuiIcon(
                                imageVector = item.icon,
                                contentDescription = null,
                                size = 22.dp,
                                tint = if (isSelected) colors.content else colors.contentMuted,
                            )
                        }
                        RuiText(
                            text = item.label,
                            style = typography.labelSm,
                            color = if (isSelected) colors.content else colors.contentMuted,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
