package calamansi.ui

import calamansi.runtime.sys.RenderTarget
import calamansi.runtime.utils.StateTracker
import org.lwjgl.util.yoga.Yoga.*

// TODO: rename
class Canvas internal constructor() {
    internal var width: Int = 0
    internal var height: Int = 0
    internal lateinit var renderTarget: RenderTarget
    internal val ygNode: Long = YGNodeNew()

    var alignContent: FlexAlign = FlexAlign.FLEX_START

    var alignItems: FlexAlign = FlexAlign.STRETCH

    var direction: FlexDirection = FlexDirection.ROW /* COLUMN <- Yoga default */

    var justifyContent: FlexJustify = FlexJustify.FLEX_START

    var wrap: FlexWrap = FlexWrap.WRAP

    private val layoutState = StateTracker.create(
        this::alignContent,
        this::alignItems,
        this::direction,
        this::justifyContent,
        this::width,
        this::height,
    )

    fun layout() {
        if (layoutState.isDirty()) {
            applyStyle(ygNode)
        }
    }

    fun calculateLayout() {
        YGNodeCalculateLayout(ygNode, YGUndefined, YGUndefined, YGDirectionLTR)
    }

    internal fun configure(width: Int, height: Int, renderTarget: RenderTarget) {
        if (this::renderTarget.isInitialized) {
            this.renderTarget.destroy()
        }

        this.width = width
        this.height = height
        this.renderTarget = renderTarget
    }

    internal fun destroy() {
        renderTarget.destroy()
        YGNodeFree(ygNode)
    }

    internal fun getLayoutLeft() = YGNodeLayoutGetLeft(ygNode)
    internal fun getLayoutTop() = YGNodeLayoutGetTop(ygNode)
}