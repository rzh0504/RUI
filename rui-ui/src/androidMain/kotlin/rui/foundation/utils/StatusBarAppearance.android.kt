package rui.foundation.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

/** One alive caller of [StatusBarAppearance], holding its requested look. */
private class AppearanceRequest {
    var darkIcons: Boolean = false
}

/**
 * Alive callers in call order, with one stack per resolved window — not per
 * View: an Android window can host several independent ComposeView roots,
 * and all of them mutate the same system bars, so every controlling root of
 * a window must share one ordering and one restoration. Within a window the
 * theme's call joins the stack first, an overriding page joins after it and
 * therefore wins.
 */
private val requestsByWindow = mutableMapOf<Window, MutableList<AppearanceRequest>>()

@Composable
@Suppress("DEPRECATION")
public actual fun StatusBarAppearance(darkIcons: Boolean) {
    val view = LocalView.current
    val request = remember { AppearanceRequest() }
    val window = remember(view) { view.findWindow() }

    if (window != null) {
        // Keep this call's look current. Only the window stack's top may
        // touch its bars, so a background caller's recomposition never
        // stomps an overlapping page's override.
        SideEffect {
            request.darkIcons = darkIcons
            if (requestsByWindow[window]?.lastOrNull() === request) {
                applyStatusBarAppearance(window, darkIcons)
            }
        }

        // Join this window's stack for as long as this call is composed, and
        // hand the bars back to the caller underneath on leaving — the
        // SideEffect-only form left an override behind once its scope
        // stopped recomposing.
        DisposableEffect(window) {
            val stack = requestsByWindow.getOrPut(window) { mutableListOf() }
            stack.remove(request) // re-join cleanly after a window swap
            stack.add(request)
            applyStatusBarAppearance(window, request.darkIcons)
            onDispose {
                if (stack.remove(request)) {
                    stack.lastOrNull()?.let {
                        applyStatusBarAppearance(window, it.darkIcons)
                    }
                    if (stack.isEmpty()) requestsByWindow.remove(window)
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun applyStatusBarAppearance(window: Window, darkIcons: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val controller = window.insetsController ?: return
        controller.setSystemBarsAppearance(
            if (darkIcons) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
        )
        controller.setSystemBarsAppearance(
            if (darkIcons) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
        )
    } else {
        var flags = window.decorView.systemUiVisibility
        flags = if (darkIcons) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags = if (darkIcons) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
        window.decorView.systemUiVisibility = flags
    }
}

/**
 * The bars this composition decorates: a dialog's own window when hosted in
 * one ([DialogWindowProvider] reachable from the view tree), else the
 * nearest activity window up the context chain.
 */
private fun View.findWindow(): Window? {
    var cursor: View = this
    while (true) {
        if (cursor is DialogWindowProvider) return cursor.window
        val parent = cursor.parent
        if (parent is DialogWindowProvider) return parent.window
        if (parent is View) {
            cursor = parent
        } else {
            break
        }
    }
    return context.findWindow()
}

private tailrec fun Context.findWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findWindow()
    else -> null
}
