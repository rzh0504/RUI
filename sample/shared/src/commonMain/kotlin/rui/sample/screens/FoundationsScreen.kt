package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import rui.foundation.ExperimentalRuiApi
import rui.foundation.GlassSurface
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.DetailScaffold
import rui.sample.ui.GroupedCard
import rui.sample.ui.RowSeparator
import rui.sample.ui.SectionLabel
import rui.theme.RuiColorScheme
import rui.theme.RuiTheme

@OptIn(ExperimentalRuiApi::class)
@Composable
public fun FoundationsScreen(onBack: () -> Unit) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    DetailScaffold(title = "Foundations", onBack = onBack) {
        SectionLabel("Type scale")
        GroupedCard {
            TypeRow("titleLg", typography.titleLg)
            RowSeparator()
            TypeRow("titleMd", typography.titleMd)
            RowSeparator()
            TypeRow("titleSm", typography.titleSm)
            RowSeparator()
            TypeRow("bodyLg", typography.bodyLg)
            RowSeparator()
            TypeRow("bodyMd", typography.bodyMd)
            RowSeparator()
            TypeRow("bodySm", typography.bodySm)
            RowSeparator()
            TypeRow("labelLg", typography.labelLg)
            RowSeparator()
            TypeRow("labelMd", typography.labelMd)
            RowSeparator()
            TypeRow("labelSm", typography.labelSm)
        }

        SectionLabel("Color tokens")
        GroupedCard {
            TokenRow("background", colors.background)
            RowSeparator()
            TokenRow("surface", colors.surface)
            RowSeparator()
            TokenRow("surfaceMuted", colors.surfaceMuted)
            RowSeparator()
            TokenRow("content", colors.content)
            RowSeparator()
            TokenRow("contentMuted", colors.contentMuted)
            RowSeparator()
            TokenRow("primary", colors.primary)
            RowSeparator()
            TokenRow("danger", colors.danger)
            RowSeparator()
            TokenRow("success", colors.success)
            RowSeparator()
            TokenRow("scrim", colors.scrim)
        }

        SectionLabel("Icons")
        GroupedCard {
            Row(
                modifier = Modifier.padding(vertical = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiIcon(imageVector = RuiIcons.Check, contentDescription = null)
                RuiIcon(imageVector = RuiIcons.Close, contentDescription = null)
                RuiIcon(imageVector = RuiIcons.ChevronLeft, contentDescription = null)
                RuiIcon(imageVector = RuiIcons.ChevronRight, contentDescription = null)
                RuiIcon(imageVector = RuiIcons.Check, contentDescription = null, tint = colors.danger)
                RuiIcon(imageVector = RuiIcons.Check, contentDescription = null, tint = colors.success)
            }
        }

        SectionLabel("Surfaces")
        GroupedCard {
            RuiSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                shape = RuiTheme.shapes.md,
                color = colors.surfaceMuted,
            ) {
                RuiText(
                    text = "Muted well",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    style = typography.bodySm,
                    color = colors.contentMuted,
                )
            }
            RuiSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                shape = RuiTheme.shapes.md,
            ) {
                RuiText(
                    text = "Plain surface — separated by contrast, not borders",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    style = typography.bodySm,
                )
            }
        }

        SectionLabel("Glass material")
        GroupedCard {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                shape = RuiTheme.shapes.md,
            ) {
                RuiText(
                    text = "Translucent tint; near-opaque where backdrop blur is unsupported.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    style = typography.bodySm,
                )
            }
        }
    }
}

@Composable
private fun TypeRow(name: String, style: androidx.compose.ui.text.TextStyle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuiText(
            text = name,
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier
                .padding(end = RuiTheme.spacing.lg)
                .widthIn(min = 72.dp),
        )
        RuiText(text = "Rui 中性设计", style = style, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TokenRow(name: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color, RuiTheme.shapes.sm),
        )
        RuiText(text = name, style = RuiTheme.typography.bodyMd, modifier = Modifier.weight(1f))
        RuiText(
            text = hexOf(color),
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentMuted,
        )
    }
}

private fun hexOf(color: Color): String {
    val argb = color.toArgb()
    return "#%06X".format(argb and 0xFFFFFF)
}
