package calamansi.ui2.control

import io.github.humbleui.skija.Canvas

abstract class Control {
    protected open val children: MutableList<Control> = mutableListOf()

    var minWidth: DimValue = DimValue.Undefined
    var minHeight: DimValue = DimValue.Undefined
    var width: DimValue = DimValue.Undefined
    var height: DimValue = DimValue.Undefined
    var maxWidth: DimValue = DimValue.Undefined
    var maxHeight: DimValue = DimValue.Undefined

    var paddingLeft: Float = 0f
    var paddingTop: Float = 0f
    var paddingRight: Float = 0f
    var paddingBottom: Float = 0f

    var marginLeft: Float = 0f
    var marginTop: Float = 0f
    var marginRight: Float = 0f
    var marginBottom: Float = 0f

    // computed after layout
    var layoutX: Float = 0f
    var layoutY: Float = 0f
    var layoutWidth: Float = 0f
    var layoutHeight: Float = 0f

    var defaultStyledBox: StyledBox by Theme.styledBox()
    var hoveredStyledBox: StyledBox by Theme.styledBox()
    var pressedStyledBox: StyledBox by Theme.styledBox()

    internal val styledBox: StyledBox
        get() = defaultStyledBox

    open fun layout(width: Float, height: Float, forceLayout: Boolean) {
        children.forEach { it.layout(width, height, forceLayout) }
    }

    open fun draw(canvas: Canvas) {
        styledBox.draw(canvas, layoutX, layoutY, layoutWidth, layoutHeight)
        children.forEach { it.draw(canvas) }
    }

    protected fun getBorderLeft(): Float {
        return when (val sb = styledBox) {
            is FlatStyledBox -> sb.borderLeftWidth
            else -> 0f
        }
    }

    protected fun getBorderTop(): Float {
        return when (val sb = styledBox) {
            is FlatStyledBox -> sb.borderTopWidth
            else -> 0f
        }
    }

    protected fun getBorderRight(): Float {
        return when (val sb = styledBox) {
            is FlatStyledBox -> sb.borderRightWidth
            else -> 0f
        }
    }

    protected fun getBorderBottom(): Float {
        return when (val sb = styledBox) {
            is FlatStyledBox -> sb.borderBottomWidth
            else -> 0f
        }
    }
}