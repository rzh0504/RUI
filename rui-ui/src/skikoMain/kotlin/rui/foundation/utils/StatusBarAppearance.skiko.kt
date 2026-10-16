package rui.foundation.utils

import androidx.compose.runtime.Composable

// Skiko-rendered windows (desktop JVM, WebAssembly) have no system bars —
// nothing to keep in sync.
@Composable
public actual fun StatusBarAppearance(darkIcons: Boolean) {
}
