package rui.foundation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * The iOS host contract of [StatusBarAppearance]: iOS owns status bar
 * styling at the view controller level, so the composable side can only
 * request — not apply. A host that wants Compose-driven status bars
 * implements the request itself, typically by overriding
 * `preferredStatusBarStyle` on the composition's root view controller and
 * calling `setNeedsStatusBarAppearanceUpdate` from [onChanged].
 */
public object IosStatusBarAppearance {
    /**
     * The look requested by the topmost alive caller of
     * [StatusBarAppearance] — the page's call wins over the theme's — or
     * `null` while no caller is composed.
     */
    public val darkIconsRequested: Boolean?
        get() = requested

    /**
     * Fired on the main thread whenever [darkIconsRequested] changes, so
     * the host can refresh its status bar appearance.
     */
    public var onChanged: () -> Unit = {}

    internal fun publish(top: Boolean?) {
        if (requested == top) return
        requested = top
        onChanged()
    }

    private var requested: Boolean? = null
}

/** One alive iOS caller of [StatusBarAppearance], holding its look. */
private class AppearanceRequest {
    var darkIcons: Boolean = false
}

/** Alive iOS callers in call order: the theme joins first, pages override. */
private val requests = mutableListOf<AppearanceRequest>()

@Composable
public actual fun StatusBarAppearance(darkIcons: Boolean) {
    val request = remember { AppearanceRequest() }

    // Keep this call's look current; only the stack's top changes what the
    // host sees, so a background caller's recomposition never stomps an
    // overlapping page's override.
    SideEffect {
        request.darkIcons = darkIcons
        if (requests.lastOrNull() === request) {
            IosStatusBarAppearance.publish(darkIcons)
        }
    }

    // Join the stack for as long as this call is composed, and hand the
    // request back to the caller underneath on leaving, mirroring the
    // Android window stack — an override never outlives its composable.
    DisposableEffect(Unit) {
        request.darkIcons = darkIcons
        requests.remove(request)
        requests.add(request)
        IosStatusBarAppearance.publish(request.darkIcons)
        onDispose {
            if (requests.remove(request)) {
                IosStatusBarAppearance.publish(requests.lastOrNull()?.darkIcons)
            }
        }
    }
}
