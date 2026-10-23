package rui.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import rui.components.RuiSlider
import rui.components.RuiSwitch
import rui.foundation.BlurBackdrop
import rui.foundation.ExperimentalRuiApi
import rui.foundation.RuiGlassMaterial
import rui.foundation.RuiLiquidGlassTuning
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

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
