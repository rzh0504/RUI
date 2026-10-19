package rui.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIStatusBarStyle
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleDefault
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.setAutoresizingMask
import rui.foundation.utils.IosStatusBarAppearance

/**
 * Composition host that forwards the composed content's status bar look:
 * iOS owns system-bar styling at the view controller level, so the embedded
 * Compose view cannot flip it — the host reads the request [StatusBarAppearance]
 * publishes and answers the system's status bar queries from it.
 */
private class StatusBarController(
    private val compose: UIViewController,
) : UIViewController(nibName = null, bundle = null) {

    override fun viewDidLoad() {
        super.viewDidLoad()
        addChildViewController(compose)
        view.addSubview(compose.view)
        compose.view.frame = view.bounds
        compose.view.autoresizingMask =
            UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        compose.didMoveToParentViewController(this)
        // Requests arrive from composition on the main thread, like this
        // call; the update marks the system for a fresh status-bar read.
        IosStatusBarAppearance.onChanged = { setNeedsStatusBarAppearanceUpdate() }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle
        get() = when (val requested = IosStatusBarAppearance.darkIconsRequested) {
            true -> UIStatusBarStyleLightContent
            false -> UIStatusBarStyleDarkContent
            null -> UIStatusBarStyleDefault
        }
        // The system style is read-only; only the getter above answers.
        set(_) {}
}

fun MainViewController() = StatusBarController(ComposeUIViewController { CatalogApp() })
