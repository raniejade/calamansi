package calamansi.ui2.control

import calamansi.gfx.Color
import calamansi.gfx.Font
import calamansi.ui.toPaint
import io.github.humbleui.skija.FontMgr
import io.github.humbleui.skija.PaintMode
import io.github.humbleui.skija.paragraph.*
import io.github.humbleui.types.Rect
import org.joml.Matrix3x2f
import org.joml.Vector2f
import org.lwjgl.util.yoga.Yoga.*
import java.util.*
import io.github.humbleui.skija.Canvas as SkijaCanvas


sealed class Element {
    internal val _id = UUID.randomUUID().toString()
    internal var parent: Element? = null

    internal abstract var canvas: Canvas?
    internal abstract val ygNode: Long
    internal abstract val localTransform: Matrix3x2f

    abstract var minWidth: Float?
    abstract var minHeight: Float?
    abstract var width: Float?
    abstract var height: Float?
    abstract var maxWidth: Float?
    abstract var maxHeight: Float?
    abstract var alignSelf: FlexAlign
    abstract var grow: Float
    abstract var shrink: Float
    abstract var layout: FlexLayout


    abstract fun requestLayout()

    abstract fun getLayoutWidth(): Float
    abstract fun getLayoutHeight(): Float
    abstract fun localToParent(x: Float, y: Float): Vector2f
    abstract fun localToGlobal(x: Float, y: Float): Vector2f

    internal abstract fun applyStyles()
    internal abstract fun fetchLayoutValues()
    internal abstract fun draw(canvas: SkijaCanvas)
}

sealed class ElementBase : Element() {
    override var canvas: Canvas? = null

    private var layoutWidth: Float? = null
    private var layoutHeight: Float? = null
    override val localTransform = Matrix3x2f()

    override var minWidth: Float? = null
        set(value) {
            field = value
            requestLayout()
        }
    override var minHeight: Float? = null
        set(value) {
            field = value
            requestLayout()
        }
    override var width: Float? = null
        set(value) {
            field = value
            requestLayout()
        }
    override var height: Float? = null
        set(value) {
            field = value
            requestLayout()
        }
    override var maxWidth: Float? = null
        set(value) {
            field = value
            requestLayout()
        }
    override var maxHeight: Float? = null
        set(value) {
            field = value
            requestLayout()
        }

    override var alignSelf: FlexAlign = FlexAlign.AUTO
        set(value) {
            field = value
            requestLayout()
        }

    override var grow: Float = 0f
        set(value) {
            field = value
            requestLayout()
        }
    override var shrink: Float = 1f
        set(value) {
            field = value
            requestLayout()
        }

    override var layout: FlexLayout = FlexLayout.RELATIVE
        set(value) {
            field = value
            requestLayout()
        }

    override fun getLayoutWidth(): Float {
        return checkNotNull(layoutWidth)
    }

    override fun getLayoutHeight(): Float {
        return checkNotNull(layoutHeight)
    }

    override fun localToParent(x: Float, y: Float): Vector2f {
        return localTransform.transformPosition(x, y, Vector2f())
    }

    override fun localToGlobal(x: Float, y: Float): Vector2f {
        val parentTransform = parent?.localTransform ?: Matrix3x2f()
        val transform = Matrix3x2f()
            .mul(parentTransform)
            .mul(localTransform)

        return transform.transformPosition(x, y, Vector2f())
    }

    override fun requestLayout() {
        canvas?.scheduleLayout(this)
    }

    override fun applyStyles() {
        minWidth?.let { YGNodeStyleSetMinWidth(ygNode, it) }
        minHeight?.let { YGNodeStyleSetMinHeight(ygNode, it) }
        width?.let { YGNodeStyleSetWidth(ygNode, it) }
        height?.let { YGNodeStyleSetHeight(ygNode, it) }
        maxWidth?.let { YGNodeStyleSetMaxWidth(ygNode, it) }
        maxHeight?.let { YGNodeStyleSetMaxHeight(ygNode, it) }
        YGNodeStyleSetAlignSelf(ygNode, alignSelf.toYGValue())
        YGNodeStyleSetFlexGrow(ygNode, grow)
        YGNodeStyleSetFlexShrink(ygNode, shrink)
        YGNodeStyleSetPositionType(
            ygNode,
            when (layout) {
                FlexLayout.RELATIVE -> YGPositionTypeRelative
                FlexLayout.ABSOLUTE -> YGPositionTypeAbsolute
            }
        )
    }

    override fun fetchLayoutValues() {
        localTransform.identity()
            .translate(YGNodeLayoutGetLeft(ygNode), YGNodeLayoutGetTop(ygNode))

        layoutWidth = YGNodeLayoutGetWidth(ygNode)
        layoutHeight = YGNodeLayoutGetHeight(ygNode)

    }

    override fun draw(canvas: SkijaCanvas) {
        val remember = canvas.localToDevice
        val pos = localToGlobal(0f, 0f)
        canvas.translate(pos.x(), pos.y())
        val rect = Rect.makeXYWH(0f, 0f, getLayoutWidth(), getLayoutHeight())
        val paint = if (this is Text) {
            Color.RED.toPaint()
        } else {
            Color.BLUE.toPaint()
        }.setMode(PaintMode.STROKE)
            .setStrokeWidth(2f)
        canvas.drawRect(rect, paint)
        canvas.setMatrix(remember)
    }
}

class Text(text: String) : ElementBase() {
    override val ygNode: Long = YGNodeNew().also {
        YGNodeSetNodeType(it, YGNodeTypeText)
    }
    var text: String = text
        set(value) {
            field = value
            requestLayout()
        }
    var font: Font = Font.defaultFont
        set(value) {
            field = value
            requestLayout()
        }
    var fontSize: Float = 12f
        set(value) {
            field = value
            requestLayout()
        }
    var fontColor: Color = Color.WHITE
        set(value) {
            field = value
            requestLayout()
        }

    private lateinit var pg: Paragraph

    init {
        YGNodeSetMeasureFunc(ygNode) { _, width, widthMode, _, _, result ->
            // TODO: probably no need to rebuild paragraph when dirty, calling pg.layout maybe enough
            val fc = FontCollection().setDefaultFontManager(FontMgr.getDefault())
            val ts = TextStyle()
                .setTypeface(font.typeface)
                .setFontSize(fontSize)
                .setForeground(fontColor.toPaint())
            val ps = ParagraphStyle()
            pg = ParagraphBuilder(ps, fc)
                .pushStyle(ts)
                .addText(text)
                .popStyle()
                .build()

            when (widthMode) {
                YGMeasureModeExactly, YGMeasureModeAtMost -> pg.layout(width)
                YGMeasureModeUndefined -> pg.layout(Float.MAX_VALUE)
                else -> error("unknown width mode.")
            }

            result.width(pg.longestLine)
            result.height(pg.height)
        }
    }

    override fun applyStyles() {
        super.applyStyles()
        YGNodeMarkDirty(ygNode)
    }

    override fun fetchLayoutValues() {
        super.fetchLayoutValues()
        pg.layout(getLayoutWidth())
    }

    override fun draw(canvas: SkijaCanvas) {
        super.draw(canvas)
        val pos = localToGlobal(0f, 0f)
        pg.paint(canvas, pos.x(), pos.y())
    }
}

class Container : ElementBase() {
    override val ygNode: Long = YGNodeNew()
    override var canvas: Canvas? = super.canvas
        set(value) {
            field = value
            children.forEach { it.canvas = field }
        }
    private val children = mutableListOf<Element>()

    var direction: FlexDirection = FlexDirection.ROW
        set(value) {
            field = value
            requestLayout()
        }
    var justifyContent: FlexJustify = FlexJustify.FLEX_START
        set(value) {
            field = value
            requestLayout()
        }
    var alignItems: FlexAlign = FlexAlign.STRETCH
        set(value) {
            field = value
            requestLayout()
        }
    var alignContent: FlexAlign = FlexAlign.STRETCH
        set(value) {
            field = value
            requestLayout()
        }
    var wrap: FlexWrap = FlexWrap.NO_WRAP
        set(value) {
            field = value
            requestLayout()
        }

    fun addChild(child: Element) {
        check(!children.contains(child))
        children.add(child)
        child.canvas = canvas
        child.parent = this
        YGNodeInsertChild(ygNode, child.ygNode, children.size - 1)
        child.requestLayout()
        requestLayout()
    }

    fun removeChild(child: Element) {
        check(children.contains(child))
        children.remove(child)
        child.canvas = null
        child.parent = null
        YGNodeRemoveChild(ygNode, child.ygNode)
        requestLayout()
    }

    fun getChildren(): List<Element> {
        return children.toList()
    }

    override fun applyStyles() {
        super.applyStyles()
        YGNodeStyleSetFlexDirection(ygNode, direction.toYGValue())
        YGNodeStyleSetJustifyContent(ygNode, justifyContent.toYGValue())
        YGNodeStyleSetAlignItems(ygNode, alignItems.toYGValue())
        YGNodeStyleSetAlignContent(ygNode, alignContent.toYGValue())
        YGNodeStyleSetFlexWrap(ygNode, wrap.toYGValue())
    }

    override fun draw(canvas: SkijaCanvas) {
        super.draw(canvas)
        // TODO: children draw handling outside
        children.forEach { it.draw(canvas) }
    }
}

abstract class Control : Element() {
    protected val root = Container()

    override var canvas: Canvas? by root::canvas
    override val ygNode: Long by root::ygNode
    override val localTransform: Matrix3x2f by root::localTransform

    override var minWidth: Float? by root::minWidth
    override var minHeight: Float? by root::minHeight
    override var width: Float? by root::width
    override var height: Float? by root::height
    override var maxWidth: Float? by root::maxWidth
    override var maxHeight: Float? by root::maxHeight
    override var alignSelf: FlexAlign by root::alignSelf
    override var grow: Float by root::grow
    override var shrink: Float by root::shrink
    override var layout: FlexLayout by root::layout

    override fun requestLayout() {
        root.requestLayout()
    }

    override fun getLayoutWidth(): Float {
        return root.getLayoutWidth()
    }

    override fun getLayoutHeight(): Float {
        return root.getLayoutHeight()
    }

    override fun localToParent(x: Float, y: Float): Vector2f {
        return root.localToParent(x, y)
    }

    override fun localToGlobal(x: Float, y: Float): Vector2f {
        return root.localToGlobal(x, y)
    }

    // TODO: how to apply styles recursively, outside or within container?
    override fun applyStyles() {
        root.applyStyles()
        for (child in root.getChildren()) {
            child.applyStyles()
        }
    }

    override fun fetchLayoutValues() {
        root.fetchLayoutValues()
        for (child in root.getChildren()) {
            child.fetchLayoutValues()
        }
    }

    override fun draw(canvas: SkijaCanvas) {
        root.draw(canvas)
    }
}

class Button(text: String = "") : Control() {
    private var _text = Text(text)

    var text: String by _text::text
    var font: Font by _text::font
    var fontSize: Float by _text::fontSize
    var fontColor: Color by _text::fontColor

    init {
        with(root) {
            direction = FlexDirection.ROW
            addChild(_text)
        }
    }
}