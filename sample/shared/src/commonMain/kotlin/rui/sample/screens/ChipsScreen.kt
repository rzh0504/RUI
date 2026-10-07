package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiChip
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

private val FilterTopics = listOf("Design", "Android", "iOS", "Web", "Desktop")
private val StartTags = listOf("Kotlin", "Compose", "Multiplatform")

@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun ChipsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    // Filter chips are multi-select: each toggles independently.
    val selectedTopics = remember { mutableStateListOf("Android") }
    // Tags are removable; the set can always be reset to its starting state.
    val tags: SnapshotStateList<String> = remember { mutableStateListOf(*StartTags.toTypedArray()) }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Filters")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        ) {
            FilterTopics.forEach { topic ->
                RuiChip(
                    text = topic,
                    selected = topic in selectedTopics,
                    onClick = {
                        if (topic in selectedTopics) selectedTopics.remove(topic)
                        else selectedTopics.add(topic)
                    },
                )
            }
        }

        SectionLabel("Leading icon")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        ) {
            RuiChip(
                text = "Checked",
                selected = true,
                onClick = {},
                leading = { RuiIcon(RuiIcons.Check, null, size = 16.dp) },
            )
            RuiChip(
                text = "Plain",
                onClick = {},
            )
        }

        SectionLabel("Tags")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        ) {
            tags.forEach { tag ->
                RuiChip(
                    text = tag,
                    onDismiss = { tags.remove(tag) },
                )
            }
        }
        if (tags.isEmpty()) {
            RuiButton(
                onClick = { tags.addAll(StartTags) },
                variant = RuiButtonVariant.Plain,
            ) {
                RuiText("Reset tags")
            }
        }

        SectionLabel("Disabled")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        ) {
            RuiChip(
                text = "Disabled filter",
                selected = false,
                onClick = {},
                enabled = false,
            )
            RuiChip(
                text = "Selected",
                selected = true,
                onClick = {},
                enabled = false,
            )
        }
    }
}
