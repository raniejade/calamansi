package calamansi.runtime.sys

sealed class PlatformStateChange {
    data class WindowSize(val width: Int, val height: Int): PlatformStateChange()
    data class FramebufferSize(val width: Int, val height: Int): PlatformStateChange()
}