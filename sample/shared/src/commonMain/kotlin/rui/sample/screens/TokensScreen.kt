package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/** The design tokens themselves: type, color, spacing, radius and icons. */
@Composable
public fun TokensScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Type scale")
        RuiListGroup {
            TypeRow("titleLg", typography.titleLg)
            RuiDivider(inset = spacing.lg)
            TypeRow("titleMd", typography.titleMd)
            RuiDivider(inset = spacing.lg)
            TypeRow("titleSm", typography.titleSm)
            RuiDivider(inset = spacing.lg)
            TypeRow("bodyLg", typography.bodyLg)
            RuiDivider(inset = spacing.lg)
            TypeRow("bodyMd", typography.bodyMd)
            RuiDivider(inset = spacing.lg)
            TypeRow("bodySm", typography.bodySm)
            RuiDivider(inset = spacing.lg)
            TypeRow("labelLg", typography.labelLg)
            RuiDivider(inset = spacing.lg)
            TypeRow("labelMd", typography.labelMd)
            RuiDivider(inset = spacing.lg)
            TypeRow("labelSm", typography.labelSm)
        }

        SectionLabel("Color tokens")
        RuiListGroup {
            TokenRow("background", colors.background)
            RuiDivider(inset = spacing.lg)
            TokenRow("surface", colors.surface)
            RuiDivider(inset = spacing.lg)
            TokenRow("surfaceMuted", colors.surfaceMuted)
            RuiDivider(inset = spacing.lg)
            TokenRow("content", colors.content)
            RuiDivider(inset = spacing.lg)
            TokenRow("contentMuted", colors.contentMuted)
            RuiDivider(inset = spacing.lg)
            TokenRow("primary", colors.primary)
            RuiDivider(inset = spacing.lg)
            TokenRow("danger", colors.danger)
            RuiDivider(inset = spacing.lg)
            TokenRow("success", colors.success)
            RuiDivider(inset = spacing.lg)
            TokenRow("scrim", colors.scrim)
        }

        SectionLabel("Spacing")
        RuiListGroup {
            listOf(
                "xs" to spacing.xs,
                "sm" to spacing.sm,
                "md" to spacing.md,
                "lg" to spacing.lg,
                "xl" to spacing.xl,
                "xxl" to spacing.xxl,
            ).forEachIndexed { index, (name, value) ->
                if (index > 0) RuiDivider(inset = spacing.lg)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RuiText(
                        text = name,
                        style = typography.labelSm,
                        color = colors.contentMuted,
                        modifier = Modifier.widthIn(min = 48.dp),
                    )
                    Box(
                        modifier = Modifier
                            .width(value)
                            .height(spacing.md)
                            .background(colors.primary, RuiTheme.shapes.sm),
                    )
                    RuiText(
                        text = "${value.value.toInt()}dp",
                        style = typography.labelSm,
                        color = colors.contentMuted,
                        modifier = Modifier.padding(start = spacing.md),
                    )
                }
            }
        }

        SectionLabel("Shape radius")
        RuiListGroup {
            listOf(
                "sm · 6" to RuiTheme.shapes.sm,
                "md · 10" to RuiTheme.shapes.md,
                "lg · 14" to RuiTheme.shapes.lg,
                "xl · 18" to RuiTheme.shapes.xl,
                "sheet · top" to RuiTheme.shapes.sheet,
                "full" to RuiTheme.shapes.full,
            ).chunked(3).forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) RuiDivider(inset = spacing.lg)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.md),
                ) {
                    row.forEach { (label, shape) ->
                        TokenCell(
                            label = label,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(colors.surfaceMuted, shape)
                                    .border(1.dp, colors.outlineSubtle, shape),
                            )
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        SectionLabel("Icons")
        RuiListGroup {
            listOf(
                "Check" to RuiIcons.Check,
                "Close" to RuiIcons.Close,
                "ChevronLeft" to RuiIcons.ChevronLeft,
                "ChevronRight" to RuiIcons.ChevronRight,
                "Grid" to RuiIcons.Grid,
                "Swatch" to RuiIcons.Swatch,
                "Search" to RuiIcons.Search,
                "Plus" to RuiIcons.Plus,
                "Minus" to RuiIcons.Minus,
                "Menu" to RuiIcons.Menu,
            ).chunked(4).forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) RuiDivider(inset = spacing.lg)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.md),
                ) {
                    row.forEach { (name, icon) ->
                        TokenCell(
                            label = name,
                            modifier = Modifier.weight(1f),
                        ) {
                            RuiIcon(imageVector = icon, contentDescription = name)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** One centered cell of a token grid: the sample above, its name below. */
@Composable
private fun TokenCell(
    label: String,
    modifier: Modifier = Modifier,
    sample: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xs),
    ) {
        sample()
        RuiText(
            text = label,
            style = RuiTheme.typography.labelSm,
            color = RuiTheme.colors.contentMuted,
            maxLines = 1,
        )
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
    val hex = (color.toArgb() and 0xFFFFFF).toString(16).uppercase()
    return "#" + hex.padStart(6, '0')
}
