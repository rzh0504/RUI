package rui.sample.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import rui.components.RuiCircularProgress
import rui.components.RuiDivider
import rui.components.RuiLinearProgress
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun ProgressScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var loadRun by remember { mutableIntStateOf(0) }
    var loadProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(loadRun) {
        if (loadRun > 0) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            ) { value, _ -> loadProgress = value }
        }
    }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Linear")
        RuiListGroup {
            Column(Modifier.padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md)) {
                Row(Modifier.fillMaxWidth()) {
                    RuiText("Downloading", style = RuiTheme.typography.titleSm)
                    Spacer(Modifier.weight(1f))
                    RuiText(
                        "65%",
                        style = RuiTheme.typography.bodySm,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiLinearProgress(progress = 0.65f)
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            Column(Modifier.padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md)) {
                RuiText("Indeterminate", style = RuiTheme.typography.titleSm)
                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiLinearProgress()
            }
            RuiDivider(inset = RuiTheme.spacing.lg)
            Column(Modifier.padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md)) {
                RuiText("Unavailable", style = RuiTheme.typography.titleSm)
                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiLinearProgress(progress = 0.65f, enabled = false)
            }
        }

        SectionLabel("Load simulation")
        RuiListGroup {
            RuiListItem(
                title = "Tap to load",
                subtitle = "Animates the bar below from 0 to 100%",
                showChevron = true,
                onClick = {
                    loadProgress = 0f
                    loadRun++
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            Column(Modifier.padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md)) {
                Row(Modifier.fillMaxWidth()) {
                    RuiText("Progress", style = RuiTheme.typography.titleSm)
                    Spacer(Modifier.weight(1f))
                    RuiText(
                        "${(loadProgress * 100).roundToInt()}%",
                        style = RuiTheme.typography.bodySm,
                        color = RuiTheme.colors.contentMuted,
                    )
                }
                Spacer(Modifier.height(RuiTheme.spacing.md))
                RuiLinearProgress(progress = loadProgress)
            }
        }

        SectionLabel("Circular")
        RuiListGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CircularDemo(progress = 0.65f, caption = "65%")
                CircularDemo(progress = null, caption = "Indeterminate")
                CircularDemo(progress = 0.65f, caption = "Unavailable", enabled = false)
            }
        }
    }
}

@Composable
private fun CircularDemo(progress: Float?, caption: String, enabled: Boolean = true) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RuiCircularProgress(progress = progress, enabled = enabled)
        Spacer(Modifier.height(RuiTheme.spacing.sm))
        RuiText(
            text = caption,
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )
    }
}
