package rui.sample.demos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.foundation.GlassSurface
import rui.foundation.ExperimentalRuiApi
import rui.theme.RuiTheme

/** Shared demo container: section title plus a token-styled surface card. */
@Composable
internal fun DemoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = RuiTheme.spacing

    Column(modifier = modifier) {
        RuiText(
            text = title,
            style = RuiTheme.typography.titleSm,
            modifier = Modifier.padding(bottom = spacing.sm),
        )
        RuiSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RuiTheme.shapes.lg,
            border = BorderStroke(RuiTheme.dimens.borderWidth, RuiTheme.colors.outlineSubtle),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun FoundationDemos(glassEnabled: Boolean) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    DemoCard(title = "Type scale") {
        TypeRow("titleLg", typography.titleLg)
        TypeRow("titleMd", typography.titleMd)
        TypeRow("titleSm", typography.titleSm)
        TypeRow("bodyLg", typography.bodyLg)
        TypeRow("bodyMd", typography.bodyMd)
        TypeRow("bodySm", typography.bodySm)
        TypeRow("labelLg", typography.labelLg)
        TypeRow("labelMd", typography.labelMd)
        TypeRow("labelSm", typography.labelSm)
    }

    DemoCard(title = "Color tokens") {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
            rui.sample.ColorSwatch("background", colors.background)
            rui.sample.ColorSwatch("surface", colors.surface)
            rui.sample.ColorSwatch("surfaceMuted", colors.surfaceMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
            rui.sample.ColorSwatch("outline", colors.outline)
            rui.sample.ColorSwatch("content", colors.content)
            rui.sample.ColorSwatch("contentMuted", colors.contentMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
            rui.sample.ColorSwatch("primary", colors.primary)
            rui.sample.ColorSwatch("danger", colors.danger)
            rui.sample.ColorSwatch("success", colors.success)
        }
    }

    DemoCard(title = "Icons") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            RuiIcon(imageVector = RuiIcons.Check, contentDescription = null)
            RuiIcon(imageVector = RuiIcons.Check, contentDescription = null, tint = colors.contentMuted)
            RuiIcon(imageVector = RuiIcons.Check, contentDescription = null, tint = colors.danger)
            RuiIcon(imageVector = RuiIcons.Check, contentDescription = null, tint = colors.success)
        }
    }

    DemoCard(title = "Surfaces") {
        RuiSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RuiTheme.shapes.lg,
            color = colors.surfaceMuted,
            border = null,
        ) {
            RuiText(
                text = "Muted well — no border",
                modifier = Modifier.padding(spacing.lg),
            )
        }
        RuiSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RuiTheme.shapes.lg,
            border = BorderStroke(RuiTheme.dimens.borderWidth, colors.outline),
        ) {
            RuiText(
                text = "Bordered card",
                modifier = Modifier.padding(spacing.lg),
            )
        }
    }

    GlassDemoCard(glassEnabled)
}

@OptIn(ExperimentalRuiApi::class)
@Composable
private fun GlassDemoCard(glassEnabled: Boolean) {
    val colors = RuiTheme.colors

    DemoCard(title = "Glass material") {
        if (glassEnabled) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RuiTheme.shapes.lg,
            ) {
                RuiText(
                    text = "Translucent glass tint — degrades to near-opaque where backdrop blur is unsupported.",
                    modifier = Modifier.padding(RuiTheme.spacing.lg),
                )
            }
        } else {
            RuiSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RuiTheme.shapes.lg,
            ) {
                RuiText(
                    text = "Glass disabled — solid surface.",
                    modifier = Modifier.padding(RuiTheme.spacing.lg),
                )
            }
        }
    }
}

@Composable
private fun TypeRow(name: String, style: androidx.compose.ui.text.TextStyle) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RuiText(
            text = name,
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.padding(end = RuiTheme.spacing.lg),
        )
        RuiText(text = "Rui 中性设计", style = style)
    }
}
