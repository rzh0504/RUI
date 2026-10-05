package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import rui.components.RuiAvatar
import rui.components.RuiAvatarSize
import rui.components.RuiDivider
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiRadioGroup
import rui.components.RuiRadioRow
import rui.components.RuiSkeleton
import rui.components.RuiSkeletonEffect
import rui.components.RuiSwitch
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun SkeletonScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var loading by rememberSaveable { mutableStateOf(true) }
    var effect by rememberSaveable { mutableStateOf(RuiSkeletonEffect.Pulse) }
    val typography = RuiTheme.typography
    val density = LocalDensity.current

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Bones stand in for content that is loading — shaped like what " +
                "they replace, animated by a breathing pulse or a highlight " +
                "sweeping left to right. Flip the switch to see what arrives.",
            style = typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        RuiListGroup {
            RuiListItem(
                title = "Loading",
                trailing = {
                    RuiSwitch(checked = loading, onCheckedChange = { loading = it })
                },
            )
        }

        RuiRadioGroup(
            value = effect,
            onValueChange = { effect = it },
        ) {
            RuiListGroup {
                RuiRadioRow(
                    value = RuiSkeletonEffect.Pulse,
                    title = "Breathing",
                    subtitle = "The whole bone pulses",
                )
                RuiDivider(inset = RuiTheme.spacing.lg)
                RuiRadioRow(
                    value = RuiSkeletonEffect.Sweep,
                    title = "Sweep",
                    subtitle = "A highlight travels left to right",
                )
            }
        }

        SectionLabel("List rows")
        RuiListGroup {
            if (loading) {
                repeat(3) { index ->
                    SkeletonRow(effect)
                    if (index < 2) RuiDivider(inset = RuiTheme.spacing.lg)
                }
            } else {
                LoadedRow(title = "Weekly report", subtitle = "Draft due Friday", initials = "WR")
                RuiDivider(inset = RuiTheme.spacing.lg)
                LoadedRow(title = "Team offsite", subtitle = "Venue shortlist attached", initials = "TO")
                RuiDivider(inset = RuiTheme.spacing.lg)
                LoadedRow(title = "Design review", subtitle = "Moved to 14:00", initials = "DR")
            }
        }

        SectionLabel("Paragraph")
        RuiListGroup {
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.sm),
                ) {
                    RuiSkeleton(modifier = Modifier.fillMaxWidth(), effect = effect)
                    RuiSkeleton(modifier = Modifier.fillMaxWidth(), effect = effect)
                    RuiSkeleton(modifier = Modifier.fillMaxWidth(0.9f), effect = effect)
                    RuiSkeleton(modifier = Modifier.fillMaxWidth(0.55f), effect = effect)
                }
            } else {
                RuiListItem(
                    title = "Release notes",
                    subtitle = "Version 2.0 ships the new picker drums, swipe " +
                        "actions and the frosted materials. The full changelog " +
                        "lives with the repository.",
                )
            }
        }
    }
}

/** Skeleton stand-in for one list row: avatar circle, title line, subtitle line. */
@Composable
private fun SkeletonRow(effect: RuiSkeletonEffect) {
    val typography = RuiTheme.typography
    val density = LocalDensity.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuiSkeleton(modifier = Modifier.size(40.dp), shape = CircleShape, effect = effect)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xs),
        ) {
            RuiSkeleton(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(with(density) { typography.titleSm.lineHeight.toDp() }),
                effect = effect,
            )
            RuiSkeleton(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(with(density) { typography.bodySm.lineHeight.toDp() }),
                effect = effect,
            )
        }
    }
}

@Composable
private fun LoadedRow(title: String, subtitle: String, initials: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuiAvatar(initials = initials, size = RuiAvatarSize.Md)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xs),
        ) {
            RuiText(text = title, style = RuiTheme.typography.titleSm)
            RuiText(
                text = subtitle,
                style = RuiTheme.typography.bodySm,
                color = RuiTheme.colors.contentMuted,
            )
        }
    }
}
