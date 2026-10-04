package rui.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.AnchorSide
import rui.components.RuiDivider
import rui.components.RuiIconButton
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.components.RuiTooltip
import rui.foundation.RuiIcon
import rui.foundation.RuiIcons
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun TooltipScreen(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    ContentColumn(contentPadding = contentPadding) {
        SectionLabel("Placement")
        RuiListGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RuiTheme.spacing.lg, vertical = RuiTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RuiTooltip(
                    text = "Tooltips float above their anchor",
                    anchor = {
                        RuiIconButton(onClick = {}) {
                            RuiIcon(imageVector = RuiIcons.Grid, contentDescription = null)
                        }
                    },
                )
                RuiTooltip(
                    text = "Or below, when asked",
                    side = AnchorSide.Bottom,
                    anchor = {
                        RuiIconButton(onClick = {}) {
                            RuiIcon(imageVector = RuiIcons.Check, contentDescription = null)
                        }
                    },
                )
            }
        }

        SectionLabel("Touch")
        RuiListGroup {
            RuiListItem(
                title = "Touch and hold",
                subtitle = "Shows on long press, dismisses itself",
                trailing = {
                    RuiTooltip(
                        text = "Mark as done",
                        anchor = {
                            RuiIconButton(onClick = {}) {
                                RuiIcon(imageVector = RuiIcons.Check, contentDescription = null)
                            }
                        },
                    )
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            RuiListItem(
                title = "Hover with a mouse",
                subtitle = "Shows after a short delay under the pointer",
                trailing = {
                    RuiTooltip(
                        text = "Dismiss",
                        anchor = {
                            RuiIconButton(onClick = {}) {
                                RuiIcon(imageVector = RuiIcons.Close, contentDescription = null)
                            }
                        },
                    )
                },
            )
        }
    }
}
