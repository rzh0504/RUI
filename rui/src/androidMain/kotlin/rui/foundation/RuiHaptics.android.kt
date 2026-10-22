package rui.foundation

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
public actual fun rememberRuiHaptics(): RuiHaptics {
    val view = LocalView.current
    return remember(view) { ViewRuiHaptics(view) }
}

private class ViewRuiHaptics(private val view: View) : RuiHaptics {
    override fun selection() = perform(selectionConstants())

    override fun impactLight() = perform(impactLightConstants())

    override fun impactMedium() = perform(impactMediumConstants(Build.VERSION.SDK_INT))

    override fun success() = perform(successConstants(Build.VERSION.SDK_INT))

    // "Withdrawn from the threshold" is the closest warning signal Android's
    // constant set offers; below it, the long-press buzz.
    override fun warning() = perform(warningConstants(Build.VERSION.SDK_INT))

    override fun error() = perform(errorConstants(Build.VERSION.SDK_INT))

    /**
     * Plays the first constant the device actually performs. `false` — no
     * vibrator, the user's haptic setting switched off, the view detached —
     * degrades to the next constant and finally stays silent; a refused buzz
     * is never forced.
     */
    private fun perform(constants: IntArray) {
        for (constant in constants) {
            if (view.performHapticFeedback(constant)) return
        }
    }
}

/**
 * The Android mapping, split per semantic class and parameterized by API
 * level so the version gates are host-testable. Every chain ends in a plainer
 * constant that predates minSdk 23, so a motor or system setting refusing the
 * rich primitive still has somewhere to degrade to.
 */
internal fun selectionConstants(): IntArray =
    intArrayOf(HapticFeedbackConstants.CLOCK_TICK)

internal fun impactLightConstants(): IntArray =
    intArrayOf(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.VIRTUAL_KEY)

internal fun impactMediumConstants(sdkInt: Int): IntArray =
    if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        intArrayOf(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE, HapticFeedbackConstants.VIRTUAL_KEY)
    } else {
        intArrayOf(HapticFeedbackConstants.VIRTUAL_KEY)
    }

internal fun successConstants(sdkInt: Int): IntArray =
    if (sdkInt >= Build.VERSION_CODES.R) {
        intArrayOf(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.CLOCK_TICK)
    } else {
        intArrayOf(HapticFeedbackConstants.CLOCK_TICK)
    }

internal fun warningConstants(sdkInt: Int): IntArray =
    if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        intArrayOf(HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE, HapticFeedbackConstants.LONG_PRESS)
    } else {
        intArrayOf(HapticFeedbackConstants.LONG_PRESS)
    }

internal fun errorConstants(sdkInt: Int): IntArray =
    if (sdkInt >= Build.VERSION_CODES.R) {
        intArrayOf(HapticFeedbackConstants.REJECT, HapticFeedbackConstants.LONG_PRESS)
    } else {
        intArrayOf(HapticFeedbackConstants.LONG_PRESS)
    }
