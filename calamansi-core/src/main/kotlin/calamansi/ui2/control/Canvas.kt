package calamansi.ui2.control

import calamansi.gfx.DefaultRenderSurface
import calamansi.gfx.RenderSurface
import calamansi.node.Node
import org.lwjgl.util.yoga.Yoga.*
import io.github.humbleui.skija.Canvas as SkijaCanvas

open class Canvas : Node() {
    // TODO: make into a resource
    //@Property
    var surface: RenderSurface = DefaultRenderSurface

    private var layoutNeeded = mutableMapOf<String, Element>()
    private var firstLayout = true

    protected var root: Element? = null
        set(value) {
            field = value
            field?.canvas = this
            firstLayout = value != null
        }

    internal fun draw(skijaCanvas: SkijaCanvas) {
        root?.draw(skijaCanvas)
    }

    internal fun layout(width: Float, height: Float, force: Boolean) {
        if (firstLayout || force) {
            recursivelyApplyStyles(root)
            YGNodeCalculateLayout(root!!.ygNode, width, height, YGDirectionLTR)
            recursivelyFetchLayoutValues(root)
            firstLayout = false
            return
        }

        if (layoutNeeded.isEmpty()) {
            return
        }

        val dirty = layoutNeeded.values.toList()
        layoutNeeded.clear()

        for (control in dirty) {
            control.applyStyles()
        }

        YGNodeCalculateLayout(root!!.ygNode, width, height, YGDirectionLTR)

        for (control in dirty) {
            control.fetchLayoutValues()
        }
    }

    internal fun scheduleLayout(control: Element) {
        layoutNeeded[control._id] = control
    }

    private fun recursivelyApplyStyles(element: Element?) {
        if (element == null) {
            return
        }

        element.applyStyles()

        if (element is Container) {
            for (child in element.getChildren()) {
                recursivelyApplyStyles(child)
            }
        }
    }

    private fun recursivelyFetchLayoutValues(element: Element?) {
        if (element == null) {
            return
        }

        element.fetchLayoutValues()

        if (element is Container) {
            for (child in element.getChildren()) {
                recursivelyFetchLayoutValues(child)
            }
        }
    }
}