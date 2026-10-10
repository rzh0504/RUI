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
    override fun selection() = perform(HapticFeedbackConstants.CLOCK_TICK)

    override fun impactLight() = perform(
        HapticFeedbackConstants.CONTEXT_CLICK,
        HapticFeedbackConstants.VIRTUAL_KEY,
    )

    override fun impactMedium() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        },
        HapticFeedbackConstants.VIRTUAL_KEY,
    )

    override fun success() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        },
        HapticFeedbackConstants.CLOCK_TICK,
    )

    // "Withdrawn from the threshold" is the closest warning signal Android's
    // constant set offers; below it, the long-press buzz.
    override fun warning() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE
        } else {
            HapticFeedbackConstants.LONG_PRESS
        },
        HapticFeedbackConstants.LONG_PRESS,
    )

    override fun error() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        },
        HapticFeedbackConstants.LONG_PRESS,
    )

    /**
     * Plays the first constant the device actually performs. `false` — no
     * vibrator, the user's haptic setting switched off, the view detached —
     * degrades to the next constant and finally stays silent; a refused buzz
     * is never forced.
     */
    private fun perform(vararg constants: Int) {
        for (constant in constants) {
            if (view.performHapticFeedback(constant)) return
        }
    }
}
