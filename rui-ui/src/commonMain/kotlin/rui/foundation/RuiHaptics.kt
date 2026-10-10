package rui.foundation

import androidx.compose.runtime.Composable

/**
 * Semantic haptic feedback, resolved to each platform's real haptics engine
 * through [rememberRuiHaptics]. Call sites speak intent — a selection tick, an
 * impact, a notification outcome — never a platform constant.
 *
 * The semantics group into four classes — selection ticks, light touches,
 * threshold knocks and notification outcomes:
 *
 * | Semantic | Class | iOS | Android (`View.performHapticFeedback`) |
 * |---|---|---|---|
 * | [selection] | selection tick | `UISelectionFeedbackGenerator` | `CLOCK_TICK` |
 * | [impactLight] | light touch | `UIImpactFeedbackGenerator` .light | `CONTEXT_CLICK`, then `VIRTUAL_KEY` |
 * | [impactMedium] | threshold knock | `UIImpactFeedbackGenerator` .medium | `GESTURE_THRESHOLD_ACTIVATE` (API 34+), then `VIRTUAL_KEY` |
 * | [success] | notification outcome | `UINotificationFeedbackGenerator` .success | `CONFIRM` (API 30+), then `CLOCK_TICK` |
 * | [warning] | notification outcome | `UINotificationFeedbackGenerator` .warning | `GESTURE_THRESHOLD_DEACTIVATE` (API 34+), then `LONG_PRESS` |
 * | [error] | notification outcome | `UINotificationFeedbackGenerator` .error | `REJECT` (API 30+), then `LONG_PRESS` |
 *
 * The Android side plays the first constant the device actually performs —
 * each call walks its fallback chain on the returned `Boolean` and ends in a
 * silent no-op when the motor is absent, the user's haptic setting is off or
 * the view can't buzz. Intensities are deliberately not promised equivalent
 * across platforms: iOS's generators are amplitude-continuous while Android's
 * constant set is discrete and version-gated, and the API 34 gesture threshold
 * constants are threshold signals standing in for weight — not a true
 * "medium impact". (Android's `HEAVY_CLICK` no longer exists in the public
 * API surface, which is why [impactMedium] borrows the threshold-activation
 * effect — the strongest neutral knock left.)
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
