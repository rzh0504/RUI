package rui.foundation.utils

import androidx.compose.runtime.Composable

/**
 * Keeps the system bar icon color in sync with the content appearance: pass
 * `darkIcons = true` while the content behind the system bars is light.
 * Call it inside the theme so toggling dark mode flips the bars in the same
 * frame as the content — with edge-to-edge content, an in-app theme toggle
 * does not reach the system bars on its own.
 *
 * On Android this drives the window's light status/navigation bar appearance;
 * on platforms where the status bar style is owned by the host view
 * controller (iOS) this is currently a no-op — style it via Info.plist.
 */
@Composable
public expect fun StatusBarAppearance(darkIcons: Boolean)
