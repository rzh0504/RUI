package rui.foundation.platform

/**
 * Whether the current platform can blur content *behind* a surface (backdrop
 * blur). Gates glass-material rendering; callers degrade to near-opaque
 * surfaces when false.
 */
public expect val isBlurSupported: Boolean
