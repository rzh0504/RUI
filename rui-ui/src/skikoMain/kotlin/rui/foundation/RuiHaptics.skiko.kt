package rui.foundation

import androidx.compose.runtime.Composable

/**
 * Skiko-rendered targets without haptics hardware (desktop JVM,
 * WebAssembly): every semantic call is a silent no-op, and the stateless
 * singleton needs no per-composition state.
 */
@Composable
public actual fun rememberRuiHaptics(): RuiHaptics = SkikoRuiHaptics

private object SkikoRuiHaptics : RuiHaptics {
    override fun selection() {}
    override fun impactLight() {}
    override fun impactMedium() {}
    override fun success() {}
    override fun warning() {}
    override fun error() {}
}
