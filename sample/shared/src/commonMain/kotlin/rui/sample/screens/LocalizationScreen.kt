package rui.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rui.components.RuiButton
import rui.components.RuiButtonVariant
import rui.components.RuiCalendarPicker
import rui.components.RuiDate
import rui.components.RuiDivider
import rui.components.RuiInlineBanner
import rui.components.RuiListItem
import rui.components.RuiListSection
import rui.components.RuiRadioGroup
import rui.components.RuiRadioRow
import rui.components.RuiStepper
import rui.components.RuiTimeOfDay
import rui.components.RuiTimePicker
import rui.foundation.RuiSurface
import rui.foundation.RuiText
import rui.sample.LanguageChoice
import rui.sample.ui.ContentColumn
import rui.sample.ui.SectionLabel
import rui.theme.RuiTheme

/**
 * The library's own strings — accessibility labels, calendar month and
 * weekday names, 12-hour period labels — switch with the language provided
 * on [rui.theme.RuiTheme]. The choice is catalog state (see [rui.sample.CatalogApp]);
 * this screen only hosts the picker and the components that localize.
 */
@Composable
public fun LocalizationScreen(
    languageChoice: LanguageChoice,
    onLanguageChoiceChange: (LanguageChoice) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var calendarDate by remember { mutableStateOf<RuiDate?>(RuiDate(2026, 10, 7)) }
    var time by remember { mutableStateOf(RuiTimeOfDay(9, 41)) }
    var guests by remember { mutableStateOf(2) }
    var bannerVisible by remember { mutableStateOf(true) }

    ContentColumn(contentPadding = contentPadding) {
        RuiText(
            text = "The strings Rui renders itself live in RuiStrings, provided " +
                "on RuiTheme. The switch retunes every one of them app-wide; " +
                "a component's explicit name parameters can still override " +
                "the language per call site.",
            style = RuiTheme.typography.bodySm,
            color = RuiTheme.colors.contentMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Language")
        RuiListSection(
            header = null,
            footer = "\"Follow system\" resolves the device's preferred " +
                "language on the sample side; the library itself never " +
                "reads platform locales.",
        ) {
            RuiRadioGroup(value = languageChoice, onValueChange = onLanguageChoiceChange) {
                Column {
                    RuiRadioRow(LanguageChoice.System, "Follow system · 跟随系统")
                    RuiDivider(inset = RuiTheme.spacing.lg)
                    RuiRadioRow(LanguageChoice.English, "English")
                    RuiDivider(inset = RuiTheme.spacing.lg)
                    RuiRadioRow(LanguageChoice.SimplifiedChinese, "简体中文")
                }
            }
        }

        SectionLabel("Calendar")
        RuiSurface {
            RuiCalendarPicker(
                value = calendarDate,
                onValueChange = { calendarDate = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RuiTheme.spacing.xs),
            )
        }

        SectionLabel("Time, 12-hour")
        RuiSurface {
            RuiTimePicker(
                value = time,
                onValueChange = { time = it },
                is24Hour = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = RuiTheme.spacing.sm),
            )
        }

        SectionLabel("Stepper and banner")
        RuiListSection(
            header = null,
            footer = "The decrease and increase segments and the banner's " +
                "dismiss affordance speak their localized accessibility " +
                "labels to screen readers.",
        ) {
            RuiListItem(
                title = "Guests",
                trailing = {
                    RuiStepper(value = guests, onValueChange = { guests = it }, range = 0..8)
                },
            )
            RuiDivider(inset = RuiTheme.spacing.lg)
            if (bannerVisible) {
                RuiInlineBanner(
                    message = if (languageChoice == LanguageChoice.SimplifiedChinese) {
                        "离线副本最后更新于 10 月 1 日。"
                    } else {
                        "The offline copy was last updated on October 1."
                    },
                    onDismiss = { bannerVisible = false },
                )
            } else {
                RuiListItem(
                    title = "Show the banner again",
                    showChevron = true,
                    onClick = { bannerVisible = true },
                )
            }
        }
    }
}
