package rui.sample.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiActionOption
import rui.components.RuiActionPanel
import rui.components.RuiBottomSheet
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.BlurBackdrop
import rui.foundation.RuiText
import rui.sample.GlassMode
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun BottomSheetScreen(
    glassMode: GlassMode,
    backdrop: BlurBackdrop,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var standardSheet by remember { mutableStateOf(false) }
    var glassSheet by remember { mutableStateOf(false) }
    var actionPanel by remember { mutableStateOf(false) }

    // The demo pages follow the Appearance glass mode.
    val glassBackdrop = if (glassMode == GlassMode.Solid) null else backdrop

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Sheets portal through RuiOverlayHost at the app root. " +
                "Drag the grabber down, tap the scrim, or press back to dismiss. " +
                "The glass sheet follows the Appearance glass mode.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
        )

        SectionLabel("Sheet")
        RuiListGroup {
            RuiListItem(
                title = "Standard sheet",
                subtitle = "Grabber, centered title, list content",
                showChevron = true,
                onClick = { standardSheet = true },
            )
            RuiListItem(
                title = "Glass sheet",
                subtitle = when (glassMode) {
                    GlassMode.Solid -> "Solid mode — opaque elevated surface"
                    GlassMode.Frosted -> "Backdrop blur behind the panel"
                },
                showChevron = true,
                onClick = { glassSheet = true },
            )
        }

        SectionLabel("Action panel")
        RuiListGroup {
            RuiListItem(
                title = "Open action panel",
                subtitle = "Options, destructive action, explicit cancel",
                showChevron = true,
                onClick = { actionPanel = true },
            )
        }
    }

    RuiActionPanel(
        visible = actionPanel,
        onDismiss = { actionPanel = false },
        title = "Move to trash?",
        message = "The draft will be removed from every device signed in.",
        actions = listOf(
            RuiActionOption(label = "Archive") { },
            RuiActionOption(label = "Move to trash", danger = true) { },
        ),
        cancelLabel = "Cancel",
        backdrop = glassBackdrop,
    )

    RuiBottomSheet(
        visible = standardSheet,
        onDismiss = { standardSheet = false },
        title = "Standard sheet",
    ) {
        RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
            RuiListItem(title = "Row one", subtitle = "The sheet sizes to its content")
            RuiListItem(title = "Row two", subtitle = "Rows are regular RuiListItems")
        }
    }

    RuiBottomSheet(
        visible = glassSheet,
        onDismiss = { glassSheet = false },
        title = "Glass sheet",
        backdrop = glassBackdrop,
    ) {
        RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
            RuiListItem(title = "Row one", subtitle = "Samples the page behind the sheet")
            RuiListItem(title = "Row two", subtitle = "Opaque surface in solid mode")
        }
    }
}
