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
import rui.components.RuiIconButton
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.DetailScaffold
import rui.sample.ui.GroupedCard
import rui.sample.ui.RowSeparator
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun ButtonsScreen(onBack: () -> Unit) {
    DetailScaffold(title = "Buttons", onBack = onBack) {
        SectionLabel("Variants")
        GroupedCard {
            ButtonRow("Primary", "The one main action") {
                RuiButton(onClick = {}) { RuiText("Continue") }
            }
            RowSeparator()
            ButtonRow("Tonal", "Secondary actions") {
                RuiButton(onClick = {}, variant = RuiButtonVariant.Tonal) { RuiText("Options") }
            }
            RowSeparator()
            ButtonRow("Plain", "Inline, low emphasis") {
                RuiButton(onClick = {}, variant = RuiButtonVariant.Plain) { RuiText("Learn more") }
            }
            RowSeparator()
            ButtonRow("Danger", "Destructive actions") {
                RuiButton(onClick = {}, variant = RuiButtonVariant.Danger) { RuiText("Delete") }
            }
            RowSeparator()
            ButtonRow("Disabled") {
                RuiButton(onClick = {}, enabled = false) { RuiText("Continue") }
            }
        }

        SectionLabel("Sizes")
        GroupedCard {
            ButtonRow("Small") {
                RuiButton(onClick = {}, size = RuiButtonSize.Sm) { RuiText("Small") }
            }
            RowSeparator()
            ButtonRow("Medium") {
                RuiButton(onClick = {}, size = RuiButtonSize.Md) { RuiText("Medium") }
            }
            RowSeparator()
            ButtonRow("Large") {
                RuiButton(onClick = {}, size = RuiButtonSize.Lg) { RuiText("Large") }
            }
        }

        SectionLabel("With icons")
        GroupedCard {
            ButtonRow("Leading icon") {
                RuiButton(
                    onClick = {},
                    leading = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
                ) {
                    RuiText("Confirm")
                }
            }
            RowSeparator()
            ButtonRow("Trailing icon") {
                RuiButton(
                    onClick = {},
                    variant = RuiButtonVariant.Tonal,
                    trailing = { RuiIcon(imageVector = RuiIcons.ChevronRight, contentDescription = null) },
                ) {
                    RuiText("Next")
                }
            }
        }

        SectionLabel("Icon buttons")
        GroupedCard {
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

@Composable
private fun ButtonRow(
    title: String,
    subtitle: String? = null,
    button: @Composable () -> Unit,
) {
    rui.sample.ui.ListRow(title = title, subtitle = subtitle, trailing = { button() })
}
