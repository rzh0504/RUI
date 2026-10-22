package rui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.composeunstyled.DialogHost
import com.composeunstyled.ModalBottomSheetHost

/** Provided by [RuiOverlayHost]; overlays log a setup note without it. */
internal val LocalRuiOverlayHostPresent = staticCompositionLocalOf { false }

/**
 * The single overlay host of the design system: dialogs ([RuiDialog]) and
 * bottom sheets ([RuiBottomSheet]) portal through it above everything the
 * app draws. Compose it once at the app root, inside [rui.theme.RuiTheme]:
 *
 * ```
 * RuiOverlayHost {
 *     AppRoot()
 * }
 * ```
 *
 * Without it the overlays still render — Unstyled falls back to a platform
 * modal — but insets, backdrop capture and scrim behavior diverge from the
 * portaled path, and each overlay logs a setup note pointing here.
 */
@Composable
public fun RuiOverlayHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DialogHost(modifier = modifier) {
        ModalBottomSheetHost {
            CompositionLocalProvider(
                LocalRuiOverlayHostPresent provides true,
                content = content,
            )
        }
    }
}

/**
 * The missing-host note for an overlay: the platform-modal fallback still
 * works, but window insets, backdrop capture and scrim behavior differ from
 * the portaled path. Logged once per overlay instance.
 */
@Composable
internal fun WarnWithoutRuiOverlayHost(component: String) {
    if (LocalRuiOverlayHostPresent.current) return
    var warned by remember { mutableStateOf(false) }
    if (!warned) {
        SideEffect {
            warned = true
            println(
                "[Rui] $component composed without RuiOverlayHost at the app root — " +
                    "falling back to a platform modal, so insets, backdrop and scrim " +
                    "behavior differ from the portaled path. Wrap the app root in RuiOverlayHost.",
            )
        }
    }
}
