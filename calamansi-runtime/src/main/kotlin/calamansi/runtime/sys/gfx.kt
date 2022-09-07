package calamansi.runtime.sys

interface Gfx {
    fun swapBuffers()
}

interface GfxDriver {
    fun start()
    fun create(window: Window): Gfx
    fun stop()
}