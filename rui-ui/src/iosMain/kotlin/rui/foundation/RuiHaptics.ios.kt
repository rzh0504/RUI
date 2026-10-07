package rui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
public actual fun rememberRuiHaptics(): RuiHaptics {
    return remember {
        val selection = UISelectionFeedbackGenerator()
        val light = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
        val medium = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
        val notification = UINotificationFeedbackGenerator()
        object : RuiHaptics {
            override fun selection() = selection.selectionChanged()
            override fun impactLight() = light.impactOccurred()
            override fun impactMedium() = medium.impactOccurred()
            override fun success() = notification.notificationOccurred(
                UINotificationFeedbackType.UINotificationFeedbackTypeSuccess,
            )
            override fun warning() = notification.notificationOccurred(
                UINotificationFeedbackType.UINotificationFeedbackTypeWarning,
            )
            override fun error() = notification.notificationOccurred(
                UINotificationFeedbackType.UINotificationFeedbackTypeError,
            )
        }
    }
}
