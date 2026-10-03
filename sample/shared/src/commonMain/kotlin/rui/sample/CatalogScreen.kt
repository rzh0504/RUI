package rui.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import rui.components.RuiSwitch
import rui.components.RuiTabGroup
import rui.foundation.RuiText
import rui.sample.demos.ButtonDemos
import rui.sample.demos.FoundationDemos
import rui.sample.demos.InputDemos
import rui.sample.demos.OverlayDemos
import rui.theme.RuiTheme

/** The four catalog sections. */
public enum class CatalogSection {
    Foundation,
    Actions,
    Inputs,
    Overlay,
}

@Composable
public fun CatalogScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    glassEnabled: Boolean,
    onGlassEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RuiTheme.colors
    val typography = RuiTheme.typography
    val spacing = RuiTheme.spacing

    var section by remember { mutableStateOf(CatalogSection.Foundation) }

    Column(modifier = modifier.safeDrawingPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xl, vertical = spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                RuiText(text = "Rui", style = typography.titleLg)
                RuiText(
                    text = "A neutral design system on Compose Unstyled",
                    style = typography.labelMd,
                    color = colors.contentMuted,
                )
            }
            RuiText(
                text = "Dark",
                style = typography.labelMd,
                color = colors.contentMuted,
                modifier = Modifier.padding(end = spacing.sm),
            )
            RuiSwitch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
        }

        RuiTabGroup(
            selectedTab = section,
            onSelectedTabChange = { section = it },
            tabs = CatalogSection.entries.toList(),
            fillWidth = true,
            labelStyle = typography.labelMd,
            label = { it.name },
            modifier = Modifier.fillMaxWidth(),
        ) { active ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.xl, vertical = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.xl),
            ) {
                when (active) {
                    CatalogSection.Foundation -> FoundationDemos(glassEnabled)
                    CatalogSection.Actions -> ButtonDemos()
                    CatalogSection.Inputs -> InputDemos()
                    CatalogSection.Overlay -> OverlayDemos(glassEnabled)
                }
            }
        }
    }
}

/** Small color swatch with its token name, used by the Foundation demos. */
@Composable
internal fun ColorSwatch(name: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color, RuiTheme.shapes.sm)
                .padding(0.dp),
        )
        RuiText(
            text = name,
            style = RuiTheme.typography.labelMd,
            color = RuiTheme.colors.contentMuted,
        )
    }
}
