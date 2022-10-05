package calamansi.ui

import calamansi.event.Event
import calamansi.input.InputState
import calamansi.input.MouseButtonStateEvent
import calamansi.input.MouseMoveEvent
import calamansi.meta.Property
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.runtime.WindowContext
import calamansi.runtime.gc.Bin
import calamansi.runtime.utils.StateTracker
import io.github.humbleui.skija.Canvas
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.util.yoga.Yoga.*

open class CanvasElement : Node() {
    protected val ygNode: Long

    init {
        val localYgNode = YGNodeNew()
        Bin.register(this) {
            YGNodeFree(localYgNode)
        }
        ygNode = localYgNode
    }

    @Property
    var alignContent: FlexAlign = FlexAlign.FLEX_START

    @Property
    var alignItems: FlexAlign = FlexAlign.STRETCH

    @Property
    var direction: FlexDirection = FlexDirection.ROW /* COLUMN */

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
    var normalStyledBox: StyledBox = EmptyStyledBox()

    @Property
    var hoveredStyledBox: StyledBox = EmptyStyledBox()

    private var _hovered = false

    context (ExecutionContext) private fun setHovered(hovered: Boolean) {
        val oldValue = _hovered
        _hovered = hovered

        if (oldValue && !hovered) {
            // old = hovered, new = not hovered
            publish(CanvasMessage.ElementExit(this))
        } else if (!oldValue && hovered) {
            // old = not hovered, new = hovered
            publish(CanvasMessage.ElementEnter(this))
        }
    }

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
        this::currentStyleBox,
    )

    fun isHovered() = _hovered

    override fun onThemeChanged(theme: Theme) {
        normalStyledBox = theme.getStyledBox(this::class, "normal")
        hoveredStyledBox = theme.getStyledBox(this::class, "hovered")

        minWidth = FlexValue.Fixed(theme.getConstant(this::class, "minWidth"))
        minHeight = FlexValue.Fixed(theme.getConstant(this::class, "minHeight"))
    }

    internal open val currentStyleBox: StyledBox
        get() = if (isHovered()) {
            hoveredStyledBox
        } else {
            normalStyledBox
        }

    context(ExecutionContext) override fun onEvent(event: Event) {
        when (event) {
            is MouseMoveEvent -> {
                val x0 = getLayoutLeft()
                val y0 = getLayoutTop()
                val x1 = x0 + getLayoutWidth()
                val y1 = y0 + getLayoutHeight()

                setHovered(event.x in x0..x1 && event.y in y0..y1)
            }

            is MouseButtonStateEvent -> {
                if (event.state == InputState.PRESSED && isHovered()) {
                    publish(CanvasMessage.ElementMousePress(this, event.button))
                } else if (event.state == InputState.RELEASED) {
                    publish(CanvasMessage.ElementMouseRelease(this, event.button))
                }
            }
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
                val insertIndex = YGNodeGetChildCount(context.canvas.ygNode)
                YGNodeInsertChild(context.canvas.ygNode, ygNode, insertIndex)
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

    protected fun getBorderLeft() = YGNodeLayoutGetBorder(ygNode, YGEdgeLeft)
    protected fun getBorderTop() = YGNodeLayoutGetBorder(ygNode, YGEdgeTop)
    protected fun getBorderRight() = YGNodeLayoutGetBorder(ygNode, YGEdgeRight)
    protected fun getBorderBottom() = YGNodeLayoutGetBorder(ygNode, YGEdgeBottom)

    protected fun getLayoutWidth() = YGNodeLayoutGetWidth(ygNode)
    protected fun getLayoutHeight() = YGNodeLayoutGetHeight(ygNode)

    internal fun getLayoutPos(): Vector2fc = Vector2f(getLayoutLeft(), getLayoutTop())

    internal open fun layout() {
        if (!layoutState.isDirty()) {
            return
        }
        applyStyle(ygNode)
        val styleBox = currentStyleBox
        if (styleBox is FlatStyledBox) {
            YGNodeStyleSetBorder(ygNode, YGEdgeLeft, styleBox.borderWidth.left)
            YGNodeStyleSetBorder(ygNode, YGEdgeTop, styleBox.borderWidth.top)
            YGNodeStyleSetBorder(ygNode, YGEdgeRight, styleBox.borderWidth.right)
            YGNodeStyleSetBorder(ygNode, YGEdgeBottom, styleBox.borderWidth.bottom)
        }
    }

    internal open fun draw(canvas: Canvas) {
        currentStyleBox.draw(
            canvas,
            0f,
            0f,
            getLayoutWidth(),
            getLayoutHeight(),
        )
    }
}