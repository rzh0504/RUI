package rui.foundation.utils

import androidx.compose.runtime.Composable

/**
 * iOS actual of [StatusBarAppearance]: the platform accepts the request, but
 * nothing applies it yet. Status bar styling is owned by the view controller
 * level and the Obj-C binding keeps the `preferredStatusBarStyle` hook
 * final, so Compose cannot flip the system bars itself, and bridging the
 * look to a host needs machinery the sample does not want to carry for now.
 * The call keeps composing so pages stay unchanged; an iOS application that
 * needs the look must style its host controller itself until a host hook
 * lands.
 */
@Composable
public actual fun StatusBarAppearance(darkIcons: Boolean) {
    // Deferred on iOS: no host-side consumer for the request exists yet, so
    // the call is deliberately an inoffensive no-op rather than a polling
    // stand-in.
}
