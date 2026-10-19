package rui.foundation.utils

import androidx.compose.runtime.Composable

/**
 * Keeps the system bar icon color in sync with the content appearance: pass
 * `darkIcons = true` while the content behind the system bars is light.
 * Call it inside the theme so toggling dark mode flips the bars in the same
 * frame as the content — with edge-to-edge content, an in-app theme toggle
 * does not reach the system bars on its own.
 *
 * Calls form an ordered stack by call order: while a page composes its own
 * call, the page's look wins; when the page leaves composition its look is
 * undone and the enclosing theme's look is re-applied, so an override never
 * survives the content that made it (the Android actual implements this;
 * platforms without owned status bars stay no-ops).
 *
 * On Android this drives the window's light status/navigation bar appearance;
 * on platforms where the status bar style is owned by the host view
 * controller (iOS) this is currently a no-op — style it via Info.plist.
 */
@Composable
public expect fun StatusBarAppearance(darkIcons: Boolean)
