package rui.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rui.components.RuiBottomSheet
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiContextMenu
import rui.components.RuiDialog
import rui.components.RuiDivider
import rui.components.RuiDropdownMenu
import rui.components.RuiDropdownMenuItem
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.BlurBackdrop
import rui.foundation.ExperimentalRuiApi
import rui.foundation.FrostedSurface
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/** A vivid field the glass samples — a flat page color shows refraction and
 *  saturation as nothing. */
private val DemoField = Brush.linearGradient(
    listOf(Color(0xFF3C87FF), Color(0xFF34C77B), Color(0xFFFF9F45)),
)

@OptIn(ExperimentalRuiApi::class)
@Composable
public fun LiquidGlassScreen(
    backdrop: BlurBackdrop,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var sheet by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "The liquid material: saturation lift, a light blur, lens " +
                "refraction at the edges and a specular highlight in place of " +
                "the hairline border. Each surface picks its material in code.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )

        SectionLabel("Frosted vs liquid")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(DemoField),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GlassPanel(
                    title = "Frosted",
                    detail = "72% tint, heavy blur,\nhairline border",
                    material = RuiGlassMaterial.Frosted,
                    backdrop = backdrop,
                    modifier = Modifier.weight(1f),
                )
                GlassPanel(
                    title = "Liquid",
                    detail = "30% tint, edge refraction,\nspecular highlight",
                    material = RuiGlassMaterial.Liquid,
                    backdrop = backdrop,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SectionLabel("Capsule lengths")
        RuiText(
            text = "barWidth grows the capsule with its items — two slots in " +
                "200dp, four in 320dp, so added items never squeeze.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
        Spacer(Modifier.height(8.dp))
        GlassField {
            MiniCapsule(
                backdrop = backdrop,
                barWidth = 200.dp,
                items = listOf(RuiIcons.Grid to "Browse", RuiIcons.Layers to "Blocks"),
            )
        }
        Spacer(Modifier.height(10.dp))
        GlassField {
            MiniCapsule(
                backdrop = backdrop,
                barWidth = 320.dp,
                items = listOf(
                    RuiIcons.Grid to "Browse",
                    RuiIcons.Layers to "Blocks",
                    RuiIcons.Swatch to "Tokens",
                    RuiIcons.Play to "Media",
                ),
            )
        }

        SectionLabel("Liquid surfaces")
        RuiListGroup {
            RuiListItem(
                title = "Liquid sheet",
                subtitle = "Refraction at the sheet's top corners",
                showChevron = true,
                onClick = { sheet = true },
            )
            RuiDropdownMenu(
                expanded = menu,
                onDismissRequest = { menu = false },
                backdrop = backdrop,
                material = RuiGlassMaterial.Liquid,
                anchor = {
                    RuiListItem(
                        title = "Liquid menu",
                        subtitle = "Anchored panel in the liquid material",
                        onClick = { menu = !menu },
                    )
                },
            ) {
                RuiDropdownMenuItem(text = "Rename", onClick = { menu = false })
                RuiDropdownMenuItem(text = "Duplicate", onClick = { menu = false })
                RuiDivider()
                RuiDropdownMenuItem(text = "Delete", onClick = { menu = false }, danger = true)
            }
            RuiContextMenu(
                backdrop = backdrop,
                material = RuiGlassMaterial.Liquid,
                menu = {
                    RuiDropdownMenuItem(text = "Pin", onClick = {})
                    RuiDivider()
                    RuiDropdownMenuItem(text = "Remove", onClick = {}, danger = true)
                },
            ) {
                RuiListItem(
                    title = "Liquid context menu",
                    subtitle = "Long-press or right-click this row",
                )
            }
            RuiListItem(
                title = "Liquid dialog",
                subtitle = "Glass panel over the scrim",
                showChevron = true,
                onClick = { dialog = true },
            )
        }
    }

    RuiBottomSheet(
        visible = sheet,
        onDismiss = { sheet = false },
        title = "Liquid glass sheet",
        backdrop = backdrop,
        material = RuiGlassMaterial.Liquid,
    ) {
        RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
            RuiListItem(title = "Row one", subtitle = "The page refracts at the sheet's edges")
            RuiListItem(title = "Row two", subtitle = "Frosted look where shaders are missing")
        }
    }

    RuiDialog(
        visible = dialog,
        onDismissRequest = { dialog = false },
        frosted = true,
        backdrop = backdrop,
        material = RuiGlassMaterial.Liquid,
        title = "Liquid glass panel",
        supportingText = "The page bends at the panel's edges; Android 12 keeps " +
            "the tint and highlight without refraction.",
        actions = {
            RuiButton(onClick = { dialog = false }, variant = RuiButtonVariant.Tonal) {
                RuiText("Got it")
            }
        },
    )
}

@OptIn(ExperimentalRuiApi::class)
@Composable
private fun GlassPanel(
    title: String,
    detail: String,
    material: RuiGlassMaterial,
    backdrop: BlurBackdrop,
    modifier: Modifier = Modifier,
) {
    FrostedSurface(
        modifier = modifier.height(120.dp),
        shape = RuiTheme.shapes.lg,
        backdrop = backdrop,
        material = material,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RuiText(text = title, style = RuiTheme.typography.titleMd)
            Spacer(Modifier.height(4.dp))
            RuiText(
                text = detail,
                style = RuiTheme.typography.labelSm,
                color = RuiTheme.colors.contentMuted,
            )
        }
    }
}

@Composable
private fun GlassField(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(DemoField),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@OptIn(ExperimentalRuiApi::class)
@Composable
private fun MiniCapsule(
    backdrop: BlurBackdrop,
    barWidth: Dp,
    items: List<Pair<ImageVector, String>>,
) {
    FrostedSurface(
        modifier = Modifier
            .width(barWidth)
            .height(52.dp),
        shape = RoundedCornerShape(percent = 50),
        backdrop = backdrop,
        material = RuiGlassMaterial.Liquid,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, (icon, label) ->
                if (index > 0) {
                    Spacer(Modifier.width(18.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RuiIcon(imageVector = icon, contentDescription = null, size = 16.dp)
                    Spacer(Modifier.width(5.dp))
                    RuiText(text = label, style = RuiTheme.typography.labelSm)
                }
            }
        }
    }
}
