package calamansi.ui

import calamansi.meta.Property
import calamansi.node.Node
import calamansi.runtime.WindowContext
import calamansi.runtime.gc.Bin
import org.jetbrains.skija.Canvas
import org.lwjgl.util.yoga.Yoga.*

open class CanvasElement : Node(), FlexElement {
    protected val ygNode = YGNodeNew()

    @Property
    override var alignContent: FlexAlign = FlexAlign.FLEX_START

    @Property
    override var alignItems: FlexAlign = FlexAlign.STRETCH

    @Property
    override var direction: FlexDirection = FlexDirection.ROW

    @Property
    override var justifyContent: FlexJustify = FlexJustify.FLEX_START

    @Property
    override var wrap: FlexWrap = FlexWrap.WRAP

    @Property
    override var layout: FlexLayout = FlexLayout.RELATIVE

    @Property
    override var position: FlexBounds = FlexBounds()

    @Property
    override var margin: FlexBounds = FlexBounds()

    @Property
    override var padding: FlexBounds = FlexBounds()

    @Property
    override var alignSelf: FlexAlign = FlexAlign.AUTO

    @Property
    override var grow: Float = 0f

    @Property
    override var shrink: Float = 1f

    @Property
    override var basis: FlexValue? = null

    @Property
    override var width: FlexValue? = null

    @Property
    override var height: FlexValue? = null

    @Property
    override var minWidth: FlexValue? = null

    @Property
    override var minHeight: FlexValue? = null

    @Property
    override var maxWidth: FlexValue? = null

    @Property
    override var maxHeight: FlexValue? = null

    @Property
    override var font: FontValue = FontValue.Inherit

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
        applyStyle(ygNode)
    }

    internal open fun draw(canvas: Canvas) = Unit
}