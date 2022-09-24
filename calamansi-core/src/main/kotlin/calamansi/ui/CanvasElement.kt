package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property
import calamansi.node.Node
import calamansi.runtime.WindowContext
import calamansi.runtime.gc.Bin
import calamansi.runtime.utils.StateTracker
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skija.Rect
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

    override var backgroundColor: Color = Color.TRANSPARENT
        set(value) {
            field = value
            backgroundPaint.close()
            backgroundPaint = value.toPaint()
        }

    private var backgroundPaint: Paint = backgroundColor.toPaint()

    @Suppress("LeakingThis")
    private val layoutState = StateTracker.create(
        this::alignContent,
        this::alignItems,
        this::direction,
        this::justifyContent,
        this::wrap,
        this::layout,
        this::position,
        this::margin,
        this::padding,
        this::alignSelf,
        this::grow,
        this::shrink,
        this::basis,
        this::width,
        this::height,
        this::minWidth,
        this::minHeight,
        this::maxWidth,
        this::maxHeight,
    )

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

    protected fun getLayoutLeft() = YGNodeLayoutGetLeft(ygNode)
    protected fun getLayoutTop() = YGNodeLayoutGetTop(ygNode)
    protected fun getLayoutRight() = YGNodeLayoutGetRight(ygNode)
    protected fun getLayoutBottom() = YGNodeLayoutGetBottom(ygNode)

    protected fun getPaddingLeft() = YGNodeLayoutGetPadding(ygNode, YGEdgeLeft)
    protected fun getPaddingTop() = YGNodeLayoutGetPadding(ygNode, YGEdgeTop)
    protected fun getPaddingRight() = YGNodeLayoutGetPadding(ygNode, YGEdgeRight)
    protected fun getPaddingBottom() = YGNodeLayoutGetPadding(ygNode, YGEdgeBottom)

    protected fun getLayoutWidth() = YGNodeLayoutGetWidth(ygNode)
    protected fun getLayoutHeight() = YGNodeLayoutGetHeight(ygNode)

    internal open fun layout() {
        if (!layoutState.isDirty()) {
            return
        }
        applyStyle(ygNode)
    }

    internal open fun draw(canvas: Canvas) {
        if (backgroundColor.a != 0) {
            canvas.drawRect(
                Rect.makeXYWH(getLayoutLeft(), getLayoutTop(), getLayoutWidth(), getLayoutHeight()),
                backgroundPaint
            )
        }
    }
}