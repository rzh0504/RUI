package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiButton
import rui.components.RuiButtonSize
import rui.components.RuiButtonVariant
import rui.components.RuiDivider
import rui.components.RuiIconButton
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun ButtonsScreen(modifier: Modifier = Modifier) {
    ContentColumn(modifier = modifier) {
        SectionLabel("Variants")
        RuiListGroup {
            RuiListItem(
                title = "Primary",
                subtitle = "The one main action",
                trailing = { RuiButton(onClick = {}) { RuiText("Continue") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Tonal",
                subtitle = "Secondary actions",
                trailing = { RuiButton(onClick = {}, variant = RuiButtonVariant.Tonal) { RuiText("Options") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Plain",
                subtitle = "Inline, low emphasis",
                trailing = { RuiButton(onClick = {}, variant = RuiButtonVariant.Plain) { RuiText("Learn more") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Danger",
                subtitle = "Destructive actions",
                trailing = { RuiButton(onClick = {}, variant = RuiButtonVariant.Danger) { RuiText("Delete") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Disabled",
                trailing = { RuiButton(onClick = {}, enabled = false) { RuiText("Continue") } },
            )
        }

        SectionLabel("Sizes")
        RuiListGroup {
            RuiListItem(
                title = "Small",
                trailing = { RuiButton(onClick = {}, size = RuiButtonSize.Sm) { RuiText("Small") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Medium",
                trailing = { RuiButton(onClick = {}, size = RuiButtonSize.Md) { RuiText("Medium") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Large",
                trailing = { RuiButton(onClick = {}, size = RuiButtonSize.Lg) { RuiText("Large") } },
            )
        }

        SectionLabel("With icons")
        RuiListGroup {
            RuiListItem(
                title = "Leading icon",
                trailing = {
                    RuiButton(
                        onClick = {},
                        leading = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
                    ) {
                        RuiText("Confirm")
                    }
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Trailing icon",
                trailing = {
                    RuiButton(
                        onClick = {},
                        variant = RuiButtonVariant.Tonal,
                        trailing = { RuiIcon(imageVector = RuiIcons.ChevronRight, contentDescription = null) },
                    ) {
                        RuiText("Next")
                    }
                },
            )
        }

        SectionLabel("Icon buttons")
        RuiListGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiIconButton(onClick = {}, size = RuiButtonSize.Sm) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close", size = 16.dp)
                }
                RuiIconButton(onClick = {}) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close")
                }
                RuiIconButton(onClick = {}, size = RuiButtonSize.Lg) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close", size = 24.dp)
                }
                RuiIconButton(onClick = {}, enabled = false) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close")
                }
            }
        }
    }
}
