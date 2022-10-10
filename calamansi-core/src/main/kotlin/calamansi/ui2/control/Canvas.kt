package calamansi.ui2.control

import calamansi.gfx.DefaultRenderSurface
import calamansi.gfx.RenderSurface
import calamansi.node.Node
import io.github.humbleui.skija.Canvas as SkijaCanvas

open class Canvas : Node() {
    // TODO: make into a resource
    //@Property
    var surface: RenderSurface = DefaultRenderSurface

    protected lateinit var root: Control

    internal fun layout(forceLayout: Boolean, width: Float, height: Float) {
        root.layout(width, height, forceLayout)
    }

    internal fun draw(skijaCanvas: SkijaCanvas) {
        root.draw(skijaCanvas)
    }
}