package rui.foundation

import android.view.HapticFeedbackConstants
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

/**
 * The Android haptic mapping with its version gates: which constants each
 * semantic plays per API level, and that every chain ends in a plainer
 * constant for motors or system settings that refuse the rich one. The
 * constants survive the JVM's android.jar stubs, so the gates are testable on
 * the host; the View's Boolean result handling itself needs a device.
 */
class RuiHapticsMappingTest {

    @Test
    fun selectionIsAPlainTickOnEveryLevel() {
        assertContentEquals(intArrayOf(HapticFeedbackConstants.CLOCK_TICK), selectionConstants())
    }

    @Test
    fun lightImpactDegradesToTheVirtualKey() {
        assertContentEquals(
            intArrayOf(HapticFeedbackConstants.CONTEXT_CLICK, HapticFeedbackConstants.VIRTUAL_KEY),
            impactLightConstants(),
        )
    }

    @Test
    fun mediumImpactIsTheThresholdKnockFromApi34() {
        // 33: the API 34 threshold constant does not exist yet.
        assertContentEquals(intArrayOf(HapticFeedbackConstants.VIRTUAL_KEY), impactMediumConstants(33))
        assertContentEquals(
            intArrayOf(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE, HapticFeedbackConstants.VIRTUAL_KEY),
            impactMediumConstants(34),
        )
    }

    @Test
    fun notificationOutcomesDegradePerApiLevel() {
        assertContentEquals(intArrayOf(HapticFeedbackConstants.CLOCK_TICK), successConstants(29))
        assertContentEquals(
            intArrayOf(HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.CLOCK_TICK),
            successConstants(30),
        )
        assertContentEquals(intArrayOf(HapticFeedbackConstants.LONG_PRESS), warningConstants(33))
        assertContentEquals(
            intArrayOf(HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE, HapticFeedbackConstants.LONG_PRESS),
            warningConstants(34),
        )
        assertContentEquals(intArrayOf(HapticFeedbackConstants.LONG_PRESS), errorConstants(29))
        assertContentEquals(
            intArrayOf(HapticFeedbackConstants.REJECT, HapticFeedbackConstants.LONG_PRESS),
            errorConstants(30),
        )
    }

    @Test
    fun everyChainEndsInAPlainConstantAvailableBelowMinSdk() {
        // CLOCK_TICK (21), VIRTUAL_KEY (5) and LONG_PRESS (3) all predate
        // minSdk 23 — the degradation floor every device can play.
        val chains = listOf(
            selectionConstants(),
            impactLightConstants(),
            impactMediumConstants(23), impactMediumConstants(34),
            successConstants(23), successConstants(30),
            warningConstants(23), warningConstants(34),
            errorConstants(23), errorConstants(30),
        )
        for (constants in chains) {
            assertTrue(constants.isNotEmpty())
            val last = constants.last()
            assertTrue(
                last == HapticFeedbackConstants.CLOCK_TICK ||
                    last == HapticFeedbackConstants.VIRTUAL_KEY ||
                    last == HapticFeedbackConstants.LONG_PRESS,
                "chain must end in a plain constant, was $last",
            )
        }
    }
}
