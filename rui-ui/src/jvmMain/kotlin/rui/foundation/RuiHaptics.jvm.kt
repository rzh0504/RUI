package rui.foundation

import androidx.compose.runtime.Composable

/**
 * Desktop has no haptics hardware, so every semantic call is a silent
 * no-op; the stateless singleton needs no per-composition state.
 */
@Composable
public actual fun rememberRuiHaptics(): RuiHaptics = DesktopRuiHaptics

private object DesktopRuiHaptics : RuiHaptics {
    override fun selection() {}
    override fun impactLight() {}
    override fun impactMedium() {}
    override fun success() {}
    override fun warning() {}
    override fun error() {}
}
