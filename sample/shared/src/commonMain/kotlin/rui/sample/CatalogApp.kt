package rui.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.composeunstyled.DialogHost
import rui.foundation.ExperimentalRuiApi
import rui.theme.RuiTheme

/**
 * Root of the catalog app: theme selection, glass toggle and the shared demo
 * screens. [DialogHost] is composed here so RuiDialog panels portal above
 * everything else in the tree.
 */
@OptIn(ExperimentalRuiApi::class)
@Composable
public fun CatalogApp() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }
    var glassEnabled by remember { mutableStateOf(true) }

    RuiTheme(darkTheme = darkTheme) {
        DialogHost {
            CatalogScreen(
                darkTheme = darkTheme,
                onDarkThemeChange = { darkTheme = it },
                glassEnabled = glassEnabled,
                onGlassEnabledChange = { glassEnabled = it },
                modifier = Modifier
                    .fillMaxSize()
                    .background(RuiTheme.colors.background),
            )
        }
    }
}
