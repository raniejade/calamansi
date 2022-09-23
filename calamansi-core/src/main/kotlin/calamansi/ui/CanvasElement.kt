package calamansi.ui

import calamansi.meta.Property
import calamansi.node.Node
import calamansi.runtime.WindowContext
import calamansi.runtime.gc.Bin
import org.jetbrains.skija.Canvas
import org.lwjgl.util.yoga.Yoga.*

open class CanvasElement : Node() {
    protected val ygNode = YGNodeNew()

    @Property
    var alignContent: FlexAlign = FlexAlign.FLEX_START

    @Property
    var alignItems: FlexAlign = FlexAlign.STRETCH

    @Property
    var direction: FlexDirection = FlexDirection.ROW

    @Property
    var justifyContent: FlexJustify = FlexJustify.FLEX_START

    @Property
    var wrap: FlexWrap = FlexWrap.WRAP

    @Property
    var layout: FlexLayout = FlexLayout.RELATIVE

    @Property
    var position: FlexBounds = FlexBounds()

    @Property
    var margin: FlexBounds = FlexBounds()

    @Property
    var padding: FlexBounds = FlexBounds()

    @Property
    var alignSelf: FlexAlign = FlexAlign.AUTO

    @Property
    var grow: Float = 0f

    @Property
    var shrink: Float = 1f

    @Property
    var basis: FlexValue? = null

    @Property
    var width: FlexValue? = null

    @Property
    var height: FlexValue? = null

    @Property
    var minWidth: FlexValue? = null

    @Property
    var minHeight: FlexValue? = null

    @Property
    var maxWidth: FlexValue? = null

    @Property
    var maxHeight: FlexValue? = null

    @Property
    var font: FontValue = FontValue.Inherit

    init {
        Bin.register(this) {
            YGNodeFree(ygNode)
        }
    }

    override fun parentChanged(old: Node?, new: Node?) {
        // unlink from old parent
        if (old is CanvasElement) {
            YGNodeRemoveChild(old.ygNode, ygNode)
        }
    }

    override fun nodeEnterTree() {
        // link to new parent
        when (val parent = parent) {
            is CanvasElement -> {
                val insertIndex = YGNodeGetChildCount(parent.ygNode)
                YGNodeInsertChild(parent.ygNode, ygNode, insertIndex)
            }

            null -> Unit
            else -> {
                val context = executionContext as WindowContext
                val insertIndex = YGNodeGetChildCount(context.yogaRoot)
                YGNodeInsertChild(context.yogaRoot, ygNode, insertIndex)
            }
        }
    }

    protected fun computeFont(): Font {
        var current: CanvasElement? = this

        while (current != null) {
            if (current.font is FontValue.Ref) {
                return (font as FontValue.Ref).ref.get()
            }
            // inherit
            current = parent as? CanvasElement
        }
        return (executionContext as WindowContext).defaultFont.get()
    }

    internal open fun applyLayout() {
        // TODO: apply flex style values

        YGNodeStyleSetFlexDirection(ygNode, direction.toYGValue())
        YGNodeStyleSetAlignItems(ygNode, alignItems.toYGValue())
        YGNodeStyleSetAlignContent(ygNode, alignContent.toYGValue())
        YGNodeStyleSetJustifyContent(ygNode, justifyContent.toYGValue())

        YGNodeStyleSetPositionType(
            ygNode,
            when (layout) {
                FlexLayout.RELATIVE -> YGPositionTypeRelative
                FlexLayout.ABSOLUTE -> YGPositionTypeAbsolute
            }
        )

        YGNodeStyleSetAlignSelf(ygNode, alignSelf.toYGValue())

        // position
        applyStylePosition(position.left, YGEdgeLeft)
        applyStylePosition(position.top, YGEdgeTop)
        applyStylePosition(position.bottom, YGEdgeBottom)
        applyStylePosition(position.right, YGEdgeRight)
    }


    private fun applyStylePosition(value: FlexValue, edge: Int) {
        when (value) {
            is FlexValue.Fixed -> {
                YGNodeStyleSetPosition(ygNode, edge, value.value)
            }

            is FlexValue.Relative -> {
                YGNodeStyleSetPositionPercent(ygNode, edge, value.pc)
            }
        }
    }

    internal open fun draw(canvas: Canvas) = Unit
}