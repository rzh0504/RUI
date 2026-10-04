package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiDisclosure
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun DisclosureScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var whatExpanded by rememberSaveable { mutableStateOf(false) }
    var detailsExpanded by rememberSaveable { mutableStateOf(true) }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Disclosure")
        RuiDisclosure(
            title = "What is Rui?",
            expanded = whatExpanded,
            onExpandedChange = { whatExpanded = it },
        ) {
            RuiText(
                text = "A neutral, cross-platform design system for Compose " +
                    "Multiplatform. It renders the same clean, restrained UI " +
                    "on Android and iOS — closest to iOS in feel, without " +
                    "imitating any platform's system chrome.",
                style = RuiTheme.typography.bodyMd,
                color = RuiTheme.colors.contentMuted,
            )
        }

        RuiDisclosure(
            title = "Design rules",
            expanded = detailsExpanded,
            onExpandedChange = { detailsExpanded = it },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm)) {
                DetailRow("Radii", "Continuous corners, picked by role")
                DetailRow("State layer", "10% content color on press")
                DetailRow("Lists", "Inset grouped cards, hairline dividers")
                DetailRow("Chrome", "Frosted bars over a shared backdrop")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    RuiText(
        text = "$label — $value",
        style = RuiTheme.typography.bodyMd,
        color = RuiTheme.colors.contentMuted,
        modifier = Modifier.fillMaxWidth(),
    )
}
