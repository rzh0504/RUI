package rui.sample.screens

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import rui.foundation.ExperimentalRuiApi
import rui.foundation.FrostedSurface
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.components.RuiDivider
import rui.components.RuiListItem
import rui.components.RuiListGroup
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@OptIn(ExperimentalRuiApi::class)
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

        SectionLabel("Icons")
        RuiListGroup {
            Row(
                modifier = Modifier.padding(vertical = spacing.md),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.xl),
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
        RuiListGroup {
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

        SectionLabel("Frosted material")
        RuiListGroup {
            FrostedSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                shape = RuiTheme.shapes.md,
            ) {
                RuiText(
                    text = "Translucent tint; degrades to near-opaque where backdrop blur is unsupported.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    style = typography.bodySm,
                )
            }
        }

        SectionLabel("Motion")
        RuiListGroup {
            MotionRow("tracking", "0.9 / 1755", RuiTheme.motion.tracking)
            RuiDivider(inset = RuiTheme.spacing.lg)
            MotionRow("settle", "0.96 / 322", RuiTheme.motion.settle)
            RuiDivider(inset = RuiTheme.spacing.lg)
            MotionRow("press", "0.6 / 987", RuiTheme.motion.press)
            RuiDivider(inset = RuiTheme.spacing.lg)
            MotionRow("pop", "0.85 / 450", RuiTheme.motion.pop)
            RuiDivider(inset = RuiTheme.spacing.lg)
            MotionRow("navigation", "1.0 / 550", RuiTheme.motion.navigation)
        }

        SectionLabel("Layout")
        RuiListGroup {
            RuiListItem(
                title = "Window width class",
                subtitle = "Material breakpoints: 600dp / 840dp",
                trailing = {
                    RuiText(
                        text = RuiTheme.windowWidthClass.name,
                        style = typography.labelLg,
                        color = colors.contentMuted,
                    )
                },
            )
        }
    }
}

/**
 * Tapping the row springs the dot across the track with the given family's
 * spec — toggling direction shows the return feel too.
 */
@Composable
private fun MotionRow(name: String, spec: String, animationSpec: SpringSpec<Float>) {
    var target by remember { mutableStateOf(0f) }
    val position by animateFloatAsState(
        targetValue = target,
        animationSpec = animationSpec,
        label = "MotionRow",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RuiTheme.dimens.touchTarget)
            .clickable { target = if (target == 1f) 0f else 1f }
            .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
    ) {
        Column(Modifier.weight(1f)) {
            RuiText(text = name, style = RuiTheme.typography.titleSm)
            RuiText(
                text = "damping / stiffness",
                style = RuiTheme.typography.bodySm,
                color = RuiTheme.colors.contentMuted,
            )
        }
        BoxWithConstraints(Modifier.width(120.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RuiTheme.spacing.lg)
                    .background(RuiTheme.colors.surfaceMuted, RuiTheme.shapes.full),
            )
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth - 16.dp) * position)
                    .size(16.dp)
                    .background(RuiTheme.colors.primary, CircleShape),
            )
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
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(RuiTheme.spacing.md),
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
