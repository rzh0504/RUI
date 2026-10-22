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
import kotlin.math.pow
import kotlin.math.roundToInt
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
import rui.foundation.RuiLiquidGlassTuning
import rui.foundation.RuiText
import rui.foundation.blurBackdrop
import rui.foundation.rememberBlurBackdrop
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.components.RuiSlider
import rui.components.RuiSwitch
import rui.theme.RuiTheme

/** A vivid field the glass samples — a flat page color shows refraction and
 *  saturation as nothing. The field owns its own capture, recorded from the
 *  pure-gradient layer beneath the glass: a glass element must never sample
 *  a capture whose recorded subtree contains the element itself — the
 *  capture's re-record references the glass's layer which references the
 *  capture, and the render thread walks the cycle to death. */
private val DemoField = Brush.linearGradient(
    listOf(Color(0xFF3C87FF), Color(0xFF34C77B), Color(0xFFFF9F45)),
)

@Composable
private fun DemoField(
    height: Dp,
    content: @Composable (BlurBackdrop) -> Unit,
) {
    val fieldBackdrop = rememberBlurBackdrop(liquid = true)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .blurBackdrop(fieldBackdrop)
                .background(DemoField),
        )
        content(fieldBackdrop)
    }
}

/** The demo's approximation of Apple's Liquid Glass: clearer, less blur,
 *  stronger edge refraction and highlight than Rui's shipped look. */
private val IosLikeTuning = RuiLiquidGlassTuning(
    tintAlpha = 0.18f,
    blurRadius = 8.dp,
    refractionHeight = 14.dp,
    saturation = 1.9f,
    highlightAlpha = 0.7f,
    chromaticAberration = true,
)

@OptIn(ExperimentalRuiApi::class)
@Composable
public fun LiquidGlassScreen(
    backdrop: BlurBackdrop,
    tuning: RuiLiquidGlassTuning,
    onTuningChange: (RuiLiquidGlassTuning) -> Unit,
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

        SectionLabel("Material tuning")
        RuiText(
            text = "Live recipe for every liquid surface on this screen — " +
                "bars and popups included. Session-scoped; restart to reset.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
        TuningRow(
            label = "Tint alpha",
            value = tuning.tintAlpha.formatted(2),
            valueRange = 0f..0.9f,
            selected = tuning.tintAlpha,
            onSelect = { onTuningChange(tuning.copy(tintAlpha = it)) },
        )
        TuningRow(
            label = "Blur",
            value = "${tuning.blurRadius.value.roundToInt()} dp",
            valueRange = 0f..40f,
            selected = tuning.blurRadius.value,
            onSelect = { onTuningChange(tuning.copy(blurRadius = it.dp)) },
        )
        TuningRow(
            label = "Refraction",
            value = "${tuning.refractionHeight.value.roundToInt()} dp",
            valueRange = 0f..24f,
            selected = tuning.refractionHeight.value,
            onSelect = { onTuningChange(tuning.copy(refractionHeight = it.dp)) },
        )
        TuningRow(
            label = "Saturation",
            value = "${tuning.saturation.formatted(2)}×",
            valueRange = 1f..3f,
            selected = tuning.saturation,
            onSelect = { onTuningChange(tuning.copy(saturation = it)) },
        )
        TuningRow(
            label = "Highlight",
            value = tuning.highlightAlpha.formatted(2),
            valueRange = 0f..1f,
            selected = tuning.highlightAlpha,
            onSelect = { onTuningChange(tuning.copy(highlightAlpha = it)) },
        )
        RuiListGroup {
            RuiListItem(
                title = "iOS 26 look",
                subtitle = "Apple's recipe — clearer, harder edges, brighter highlight",
                onClick = { onTuningChange(IosLikeTuning) },
            )
            RuiListItem(
                title = "Chromatic aberration",
                subtitle = "Per-channel lens dispersion (Android 13+ / Skia)",
                trailing = {
                    RuiSwitch(
                        checked = tuning.chromaticAberration,
                        onCheckedChange = { onTuningChange(tuning.copy(chromaticAberration = it)) },
                    )
                },
            )
            RuiListItem(
                title = "Reset to defaults",
                onClick = { onTuningChange(RuiLiquidGlassTuning()) },
            )
        }

        SectionLabel("Frosted vs liquid")
        DemoField(height = 200.dp) { fieldBackdrop ->
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
                    backdrop = fieldBackdrop,
                    modifier = Modifier.weight(1f),
                )
                GlassPanel(
                    title = "Liquid",
                    detail = "30% tint, edge refraction,\nspecular highlight",
                    material = RuiGlassMaterial.Liquid,
                    backdrop = fieldBackdrop,
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
        DemoField(height = 76.dp) { fieldBackdrop ->
            MiniCapsule(
                backdrop = fieldBackdrop,
                barWidth = 200.dp,
                items = listOf(RuiIcons.Grid to "Browse", RuiIcons.Layers to "Blocks"),
            )
        }
        Spacer(Modifier.height(10.dp))
        DemoField(height = 76.dp) { fieldBackdrop ->
            MiniCapsule(
                backdrop = fieldBackdrop,
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

@Composable
private fun TuningRow(
    label: String,
    value: String,
    valueRange: ClosedFloatingPointRange<Float>,
    selected: Float,
    onSelect: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RuiTheme.spacing.lg),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            RuiText(
                text = label,
                style = RuiTheme.typography.labelMd,
                modifier = Modifier.weight(1f),
            )
            RuiText(
                text = value,
                style = RuiTheme.typography.labelSm,
                color = RuiTheme.colors.contentMuted,
            )
        }
        RuiSlider(value = selected, onValueChange = onSelect, valueRange = valueRange)
    }
}

/** Multiplatform float formatting — String.format is JVM-only. */
private fun Float.formatted(digits: Int): String {
    val factor = 10.0.pow(digits)
    return (this * factor).roundToInt().div(factor).toString()
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
