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

/** One alive caller of [StatusBarAppearance], holding its requested look. */
private class AppearanceRequest {
    var darkIcons: Boolean = false
}

/**
 * Alive callers in call order, with one stack per host window: decorations
 * of different windows (a dialog's own window, split-screen activities) are
 * independent, so a process-global stack would interleave them — a
 * recomposition in one window could be suppressed by another window owning
 * the tail, and disposal would hand one window's bars to another's caller.
 * Within a window the theme's call joins the stack first, an overriding page
 * joins after it and therefore wins.
 */
private val requestsByWindow = mutableMapOf<View, MutableList<AppearanceRequest>>()

@Composable
@Suppress("DEPRECATION")
public actual fun StatusBarAppearance(darkIcons: Boolean) {
    val view = LocalView.current
    val request = remember { AppearanceRequest() }

    // Keep this call's look current. Only the window stack's top may touch
    // its bars, so a background caller's recomposition never stomps an
    // overlapping page's override.
    SideEffect {
        request.darkIcons = darkIcons
        if (requestsByWindow[view]?.lastOrNull() === request) {
            applyStatusBarAppearance(view, darkIcons)
        }
    }

    // Join this window's stack for as long as this call is composed, and
    // hand the bars back to the caller underneath on leaving — the
    // SideEffect-only form left an override behind once its scope stopped
    // recomposing.
    DisposableEffect(view) {
        val stack = requestsByWindow.getOrPut(view) { mutableListOf() }
        stack.remove(request) // re-join cleanly after a view swap
        stack.add(request)
        applyStatusBarAppearance(view, request.darkIcons)
        onDispose {
            if (stack.remove(request)) {
                stack.lastOrNull()?.let { applyStatusBarAppearance(view, it.darkIcons) }
                if (stack.isEmpty()) requestsByWindow.remove(view)
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun applyStatusBarAppearance(view: View, darkIcons: Boolean) {
    val window = view.context.findWindow() ?: return
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

private tailrec fun Context.findWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findWindow()
    else -> null
}
