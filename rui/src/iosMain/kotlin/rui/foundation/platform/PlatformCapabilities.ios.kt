package rui.foundation.platform

// Compose Multiplatform renders iOS through Skia, which implements blur at
// every supported deployment target.
public actual val isBlurSupported: Boolean
    get() = true
