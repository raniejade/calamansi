package calamansi.runtime.sys

internal sealed class PlatformStateChange {
    data class WindowSize(val width: Int, val height: Int): PlatformStateChange()
    data class FramebufferSize(val width: Int, val height: Int): PlatformStateChange()
}