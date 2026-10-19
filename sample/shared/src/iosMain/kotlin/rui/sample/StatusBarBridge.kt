package rui.sample

import rui.foundation.utils.IosStatusBarAppearance

/**
 * Swift-visible mirror of [IosStatusBarAppearance]: rui-ui's own host object
 * is not part of the sample framework's exported API, and the status bar
 * style lives on the SwiftUI side of the app, so the sample bridges the
 * published look with an integer that survives the Obj-C ABI untouched.
 */
public object StatusBarBridge {
    /** What the composed content asks for: 1 dark icons, 0 light icons, -1 no request. */
    public val request: Int
        get() = when (IosStatusBarAppearance.darkIconsRequested) {
            true -> 1
            false -> 0
            null -> -1
        }
}
