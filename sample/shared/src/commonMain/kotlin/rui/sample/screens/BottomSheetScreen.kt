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
import rui.components.RuiBottomSheet
import rui.components.RuiListGroup
import rui.components.RuiListItem
import rui.foundation.BlurBackdrop
import rui.foundation.RuiText
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

@Composable
public fun BottomSheetScreen(
    backdrop: BlurBackdrop,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var standardSheet by remember { mutableStateOf(false) }
    var frostedSheet by remember { mutableStateOf(false) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "Sheets portal through ModalBottomSheetHost at the app root. " +
                "Drag the grabber down, tap the scrim, or press back to dismiss.",
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
                title = "Frosted sheet",
                subtitle = "Backdrop blur behind the panel",
                showChevron = true,
                onClick = { frostedSheet = true },
            )
        }
    }

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
        visible = frostedSheet,
        onDismiss = { frostedSheet = false },
        title = "Frosted sheet",
        backdrop = backdrop,
    ) {
        RuiListGroup(Modifier.padding(horizontal = RuiTheme.spacing.lg)) {
            RuiListItem(title = "Row one", subtitle = "Blur samples the page behind the sheet")
            RuiListItem(title = "Row two", subtitle = "Solid surface on unsupported platforms")
        }
    }
}
