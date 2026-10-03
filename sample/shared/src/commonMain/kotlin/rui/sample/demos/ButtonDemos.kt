package rui.sample.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import rui.theme.RuiTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import rui.components.RuiButton
import rui.components.RuiButtonSize
import rui.components.RuiButtonVariant
import rui.components.RuiIconButton
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.SampleIcons

@Composable
internal fun ButtonDemos() {
    DemoCard(title = "Variants") {
        Row(horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md)) {
            RuiButton(onClick = {}) { RuiText("Primary") }
            RuiButton(onClick = {}, variant = RuiButtonVariant.Secondary) { RuiText("Secondary") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md)) {
            RuiButton(onClick = {}, variant = RuiButtonVariant.Ghost) { RuiText("Ghost") }
            RuiButton(onClick = {}, variant = RuiButtonVariant.Danger) { RuiText("Danger") }
        }
    }

    DemoCard(title = "Sizes") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        ) {
            RuiButton(onClick = {}, size = RuiButtonSize.Sm) { RuiText("Small") }
            RuiButton(onClick = {}, size = RuiButtonSize.Md) { RuiText("Medium") }
            RuiButton(onClick = {}, size = RuiButtonSize.Lg) { RuiText("Large") }
        }
    }

    DemoCard(title = "With icons") {
        RuiButton(
            onClick = {},
            leading = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
        ) {
            RuiText("Confirm")
        }
        RuiButton(
            onClick = {},
            variant = RuiButtonVariant.Secondary,
            trailing = { RuiIcon(imageVector = SampleIcons.Close, contentDescription = null) },
        ) {
            RuiText("Dismiss")
        }
    }

    DemoCard(title = "Disabled") {
        Row(horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md)) {
            RuiButton(onClick = {}, enabled = false) { RuiText("Primary") }
            RuiButton(onClick = {}, enabled = false, variant = RuiButtonVariant.Secondary) {
                RuiText("Secondary")
            }
            RuiButton(onClick = {}, enabled = false, variant = RuiButtonVariant.Ghost) {
                RuiText("Ghost")
            }
        }
    }

    DemoCard(title = "Icon buttons") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        ) {
            RuiIconButton(onClick = {}, size = RuiButtonSize.Sm) {
                RuiIcon(imageVector = SampleIcons.Close, contentDescription = "Close", size = 16.dp)
            }
            RuiIconButton(onClick = {}) {
                RuiIcon(imageVector = SampleIcons.Close, contentDescription = "Close")
            }
            RuiIconButton(onClick = {}, size = RuiButtonSize.Lg, variant = RuiButtonVariant.Secondary) {
                RuiIcon(imageVector = SampleIcons.Close, contentDescription = "Close", size = 24.dp)
            }
            RuiIconButton(onClick = {}, enabled = false) {
                RuiIcon(imageVector = SampleIcons.Close, contentDescription = "Close")
            }
        }
    }
}
