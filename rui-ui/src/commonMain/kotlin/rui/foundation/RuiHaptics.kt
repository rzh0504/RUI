package rui.foundation

import androidx.compose.runtime.Composable

/**
 * Semantic haptic feedback, resolved to each platform's real haptics engine
 * through [rememberRuiHaptics]. Call sites speak intent — a selection tick, an
 * impact, a notification outcome — never a platform constant.
 *
 * | Semantic | iOS | Android (`View.performHapticFeedback`) |
 * |---|---|---|
 * | [selection] | `UISelectionFeedbackGenerator` | `CLOCK_TICK` |
 * | [impactLight] | `UIImpactFeedbackGenerator` .light | `CONTEXT_CLICK` |
 * | [impactMedium] | `UIImpactFeedbackGenerator` .medium | `GESTURE_THRESHOLD_ACTIVATE` (API 34+, falls back to `VIRTUAL_KEY`) |
 * | [success] | `UINotificationFeedbackGenerator` .success | `CONFIRM` (API 30+, falls back to `CLOCK_TICK`) |
 * | [warning] | `UINotificationFeedbackGenerator` .warning | `GESTURE_THRESHOLD_DEACTIVATE` (API 34+, falls back to `LONG_PRESS`) |
 * | [error] | `UINotificationFeedbackGenerator` .error | `REJECT` (API 30+, falls back to `LONG_PRESS`) |
 *
 * Android's `HEAVY_CLICK` no longer exists in the public API surface, so
 * [impactMedium] borrows the threshold-activation effect — the strongest
 * neutral knock left — and `GESTURE_THRESHOLD_DEACTIVATE`, the "withdrawn"
 * signal, stands in for warnings.
 *
 * There is deliberately no `LocalRuiHaptics`: the injection point is the
 * component parameter or the call site's own [rememberRuiHaptics], and no
 * environment-level override has earned its complexity yet.
 */
public interface RuiHaptics {
    /** A light tick per item stepped over: wheel rows, segments, chips. */
    public fun selection()

    /** A light tap against something. */
    public fun impactLight()

    /** A knock with weight to it — a threshold moment. */
    public fun impactMedium()

    /** A task succeeded. */
    public fun success()

    /** Something is off but recoverable. */
    public fun warning()

    /** Something failed. */
    public fun error()
}

/**
 * Compose entry point for [RuiHaptics]. Platform-dependent under the hood —
 * Android drives haptics through a `View`, iOS through UIKit's feedback
 * generators — so instances are remembered per composition.
 */
@Composable
public expect fun rememberRuiHaptics(): RuiHaptics
