package rui.foundation.platform

// Compose Multiplatform renders desktop through Skia, which implements blur
// on every supported desktop platform.
public actual val isBlurSupported: Boolean
    get() = true
