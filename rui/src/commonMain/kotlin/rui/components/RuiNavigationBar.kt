package rui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import rui.foundation.blurSurface
import rui.foundation.BlurBackdrop
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiIcon
import rui.foundation.RuiText
import rui.foundation.liquidGlassSurface
import rui.foundation.liquidSource
import rui.foundation.platform.isBlurSupported
import rui.foundation.ruiMinimumTouchTarget
import rui.theme.RuiTheme

/** One destination of [RuiNavigationBar]. */
public data class RuiNavigationBarItem(
    public val label: String,
    public val icon: ImageVector? = null,
)

/**
 * The bottom navigation bar. The bar shares the page
 * [RuiTheme.colors.background] color, like the top bar — with a [backdrop]
 * scrolled content is only revealed through the glass (frosted by default,
 * liquid with `material = RuiGlassMaterial.Liquid`); without one it is an
 * opaque page-background strip. Place inside [RuiScaffold]'s `bottomBar`
 * slot.
 *
 * Items are exposed as tabs, and the current destination is marked selected
 * for assistive technology. [selected] outside `items.indices` selects
 * nothing; an empty [items] renders an empty bar; tapping the already
 * selected tab re-invokes [onSelect] with its index, so scroll-to-top
 * patterns keep working.
 */
@Composable
public fun RuiNavigationBar(
    items: List<RuiNavigationBarItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BlurBackdrop? = null,
    material: RuiGlassMaterial = RuiGlassMaterial.Frosted,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography

    val barBackground = when {
        backdrop != null && material == RuiGlassMaterial.Liquid && backdrop.liquidSource != null && isBlurSupported ->
            Modifier.liquidGlassSurface(
                backdrop = backdrop,
                tint = colors.background,
            )
        backdrop != null && isBlurSupported ->
            Modifier.blurSurface(
                backdrop = backdrop,
                radius = 32.dp,
                tint = colors.background.copy(alpha = 0.65f),
            )
        else -> Modifier.background(colors.background)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(barBackground)
            // Same contract as the floating bar: the bar's own strip swallows
            // taps so they never reach the content scrolling beneath it.
            .pointerInput(Unit) {
                detectTapGestures { }
            },
    ) {
        RuiDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = 56.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .ruiMinimumTouchTarget()
                        // No press indication: iOS tab bars don't ripple.
                        .selectable(
                            selected = isSelected,
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                        ) { onSelect(index) },
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
