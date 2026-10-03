package rui.foundation.platform

import android.os.Build

public actual val isBlurSupported: Boolean
    get() = Build.VERSION.SDK_INT >= 31
