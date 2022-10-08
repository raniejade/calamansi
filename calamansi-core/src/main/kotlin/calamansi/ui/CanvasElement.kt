package calamansi.ui

import calamansi.input.InputEvent
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
        normalStyledBox = theme.getStyledBox(this::class, "default")
        hoveredStyledBox = theme.getStyledBox(this::class, "hovered")
    }

    fun requestFocus(focus: Boolean) {
        if (!isFocusable()) {
            return
        }

        if (focus && !isFocused()) {
            (executionContext as WindowContext).requestFocus(this)
        } else if (!focus && isFocused()) {
            (executionContext as WindowContext).requestFocus(null)
        }
    }

    fun isFocused(): Boolean {
        return (executionContext as WindowContext).isFocused(this)
    }

    internal open val currentStyleBox: StyledBox
        get() = if (isHovered()) {
            hoveredStyledBox
        } else {
            normalStyledBox
        }

    context(ExecutionContext) override fun onGuiEvent(event: InputEvent) {
        when (event) {
            is MouseMoveEvent -> {
                val x0 = getLayoutLeft()
                val y0 = getLayoutTop()
                val x1 = x0 + getLayoutWidth()
                val y1 = y0 + getLayoutHeight()
                setHovered(event.x in x0..x1 && event.y in y0..y1)
            }

            is MouseButtonStateEvent -> {
                if (isHovered()) {
                    if (event.state == InputState.PRESSED) {
                        publish(CanvasMessage.ElementMousePress(this, event.button))
                        val wc = (executionContext as WindowContext)
                        if (wc.shouldLoseFocus(this)) {
                            wc.requestFocus(null)
                        }
                    } else if (event.state == InputState.RELEASED) {
                        publish(CanvasMessage.ElementMouseRelease(this, event.button))

                        if (isFocusable()) {
                            requestFocus(true)
                        }
                    }
                    event.consume()
                }
            }

            else -> {
                // do nothing
            }
        }
    }

    protected open fun isFocusable(): Boolean = false

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

    protected fun getLayoutLeft() = _layoutLeft
    protected fun getLayoutTop() = _layoutTop
    protected fun getLayoutRight() = _layoutRight
    protected fun getLayoutBottom() = _layoutBottom

    protected fun getPaddingLeft() = _paddingLeft
    protected fun getPaddingTop() = _paddingTop
    protected fun getPaddingRight() = _paddingRight
    protected fun getPaddingBottom() = _paddingBottom

    protected fun getBorderLeft() = _borderLeft
    protected fun getBorderTop() = _borderTop
    protected fun getBorderRight() = _borderRight
    protected fun getBorderBottom() = _borderBottom

    protected fun getLayoutWidth() = _layoutWidth
    protected fun getLayoutHeight() = _layoutHeight

    protected fun getAvailableWidth() = _layoutWidth - _borderLeft - _borderRight - _paddingLeft - _paddingRight
    protected fun getAvailableHeight() = _layoutHeight - _borderTop - _borderBottom - _paddingTop - _paddingBottom

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

    private var _layoutLeft: Float = 0f
    private var _layoutTop: Float = 0f
    private var _layoutRight: Float = 0f
    private var _layoutBottom: Float = 0f

    private var _paddingLeft: Float = 0f
    private var _paddingTop: Float = 0f
    private var _paddingRight: Float = 0f
    private var _paddingBottom: Float = 0f

    private var _borderLeft: Float = 0f
    private var _borderTop: Float = 0f
    private var _borderRight: Float = 0f
    private var _borderBottom: Float = 0f

    private var _layoutWidth: Float = 0f
    private var _layoutHeight: Float = 0f


    internal fun fetchLayoutValues() {
        _layoutLeft = YGNodeLayoutGetLeft(ygNode)
        _layoutTop = YGNodeLayoutGetTop(ygNode)
        _layoutRight = YGNodeLayoutGetRight(ygNode)
        _layoutBottom = YGNodeLayoutGetBottom(ygNode)

        _paddingLeft = YGNodeLayoutGetPadding(ygNode, YGEdgeLeft)
        _paddingTop = YGNodeLayoutGetPadding(ygNode, YGEdgeTop)
        _paddingRight = YGNodeLayoutGetPadding(ygNode, YGEdgeRight)
        _paddingBottom = YGNodeLayoutGetPadding(ygNode, YGEdgeBottom)

        _borderLeft = YGNodeLayoutGetBorder(ygNode, YGEdgeLeft)
        _borderTop = YGNodeLayoutGetBorder(ygNode, YGEdgeTop)
        _borderRight = YGNodeLayoutGetBorder(ygNode, YGEdgeRight)
        _borderBottom = YGNodeLayoutGetBorder(ygNode, YGEdgeBottom)

        _layoutWidth = YGNodeLayoutGetWidth(ygNode)
        _layoutHeight = YGNodeLayoutGetHeight(ygNode)
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

    context(ExecutionContext) internal fun publishFocusMessage() {
        publish(CanvasMessage.ElementFocus(this))
    }

    context(ExecutionContext) internal fun publishUnFocusMessage() {
        publish(CanvasMessage.ElementUnFocus(this))
    }
}