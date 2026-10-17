package rui.foundation.platform

// Compose Multiplatform renders the web through skiko's WebGL backend,
// which implements RenderEffect blur — verified against the catalog's
// glass bars in a real browser.
public actual val isBlurSupported: Boolean
    get() = true
