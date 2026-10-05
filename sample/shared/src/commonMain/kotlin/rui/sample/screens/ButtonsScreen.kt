package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import rui.components.RuiAvatar
import rui.components.RuiAvatarSize
import rui.components.RuiBadge
import rui.components.RuiButton
import rui.components.RuiButtonSize
import rui.components.RuiButtonVariant
import rui.components.RuiDivider
import rui.components.RuiIconButton
import rui.components.RuiLoadingButton
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun ButtonsScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(submitting) {
        if (submitting) {
            delay(2000)
            submitting = false
        }
    }

    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Variants")
        RuiListGroup {
            RuiListItem(
                title = "Primary",
                subtitle = "The one main action",
                trailing = { RuiButton(onClick = {}) { RuiText("Continue") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Tonal",
                subtitle = "Secondary actions",
                trailing = { RuiButton(onClick = {}, variant = RuiButtonVariant.Tonal) { RuiText("Options") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Plain",
                subtitle = "Inline, low emphasis",
                trailing = { RuiButton(onClick = {}, variant = RuiButtonVariant.Plain) { RuiText("Learn more") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Danger",
                subtitle = "Destructive actions",
                trailing = { RuiButton(onClick = {}, variant = RuiButtonVariant.Danger) { RuiText("Delete") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Disabled",
                trailing = { RuiButton(onClick = {}, enabled = false) { RuiText("Continue") } },
            )
        }

        SectionLabel("Sizes")
        RuiListGroup {
            RuiListItem(
                title = "Small",
                trailing = { RuiButton(onClick = {}, size = RuiButtonSize.Sm) { RuiText("Small") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Medium",
                trailing = { RuiButton(onClick = {}, size = RuiButtonSize.Md) { RuiText("Medium") } },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Large",
                trailing = { RuiButton(onClick = {}, size = RuiButtonSize.Lg) { RuiText("Large") } },
            )
        }

        SectionLabel("With icons")
        RuiListGroup {
            RuiListItem(
                title = "Leading icon",
                trailing = {
                    RuiButton(
                        onClick = {},
                        leading = { RuiIcon(imageVector = RuiIcons.Check, contentDescription = null) },
                    ) {
                        RuiText("Confirm")
                    }
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Trailing icon",
                trailing = {
                    RuiButton(
                        onClick = {},
                        variant = RuiButtonVariant.Tonal,
                        trailing = { RuiIcon(imageVector = RuiIcons.ChevronRight, contentDescription = null) },
                    ) {
                        RuiText("Next")
                    }
                },
            )
        }

        SectionLabel("Icon buttons")
        RuiListGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiIconButton(onClick = {}, size = RuiButtonSize.Sm) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close", size = 16.dp)
                }
                RuiIconButton(onClick = {}) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close")
                }
                RuiIconButton(onClick = {}, size = RuiButtonSize.Lg) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close", size = 24.dp)
                }
                RuiIconButton(onClick = {}, enabled = false) {
                    RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Close")
                }
            }
        }

        SectionLabel("Badge")
        RuiListGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(RuiTheme.spacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    RuiIconButton(onClick = {}) {
                        RuiIcon(imageVector = RuiIcons.Grid, contentDescription = "Inbox")
                    }
                    RuiBadge(
                        text = "3",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
                    )
                }
                Box {
                    RuiIconButton(onClick = {}) {
                        RuiIcon(imageVector = RuiIcons.Close, contentDescription = "Alerts")
                    }
                    RuiBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = 3.dp),
                    )
                }
                Box {
                    RuiAvatar(initials = "ZR", size = RuiAvatarSize.Md)
                    RuiBadge(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp),
                    )
                }
            }
        }

        SectionLabel("Loading button")
        RuiListGroup {
            RuiListItem(
                title = "Simulate submit",
                subtitle = "Disables and shows a spinner for two seconds",
                trailing = {
                    RuiLoadingButton(
                        onClick = { submitting = true },
                        isLoading = submitting,
                    ) {
                        RuiText("Submit")
                    }
                },
            )
        }
    }
}
