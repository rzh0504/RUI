package rui.foundation.utils

import androidx.compose.runtime.Composable

@Composable
public actual fun StatusBarAppearance(darkIcons: Boolean) {
    // iOS: the status bar style is owned by the hosting view controller
    // (UIStatusBarStyle via Info.plist / the ComposeUIViewController) — not
    // wired yet, so leave the system appearance untouched.
}
