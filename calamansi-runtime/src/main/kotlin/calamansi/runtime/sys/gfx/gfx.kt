package calamansi.runtime.sys.gfx

import calamansi.runtime.sys.window.Window

interface Surface {
    fun destroy()
}

interface Frame {
    fun submit()
}

interface Gfx {
    fun createSurface(window: Window): Surface
    fun startFrame(surface: Surface): Frame
}

interface GfxDriver {
    fun init()
    fun create(): Gfx
    fun shutdown()
}