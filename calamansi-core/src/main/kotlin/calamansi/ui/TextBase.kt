package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property
import calamansi.runtime.WindowContext
import calamansi.runtime.utils.StateTracker
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skija.TextBlob
import org.jetbrains.skija.shaper.Shaper
import org.lwjgl.util.yoga.YGSize
import org.lwjgl.util.yoga.Yoga
import org.lwjgl.util.yoga.Yoga.*

// TODO: support padding
abstract class TextBase(text: String) : CanvasElement() {
    private lateinit var blob: TextBlob

    init {
        YGNodeSetMeasureFunc(ygNode) { _, _, _, _, _, size ->
            size.width(blob.blockBounds.width)
            size.height(blob.blockBounds.height)
        }
    }

    @Property
    var text: String = text

    @Property
    var fontSize: Float = 12f

    @Property
    var fontColor: Color = Color.WHITE
        set(value) {
            field = value
            textPaint.close()
            textPaint = value.toPaint()
        }

    @Property
    var font: Font? = null

    private var textPaint: Paint = fontColor.toPaint()

    @Suppress("LeakingThis")
    private val textLayoutState = StateTracker.create(
        this::text,
        this::font,
        this::fontSize,
        this::width,
    )

    override fun layout() {
        val font = font ?: (executionContext as WindowContext).defaultFont
        if (textLayoutState.isDirty()) {
            YGNodeMarkDirty(ygNode)
            if (::blob.isInitialized) {
                blob.close()
            }

            // TODO: how to support percentage based width and height?
            blob = Shaper.make().use { shaper ->
                val width = width
                if (width is FlexValue.Fixed) {
                    shaper.shape(text, font.makeSkijaFont(fontSize), width.value)
                } else {
                    shaper.shape(text, font.makeSkijaFont(fontSize))
                }!!
            }
        }

        super.layout()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawTextBlob(blob, getLayoutLeft() + getPaddingLeft(), getLayoutTop() + getPaddingTop(), textPaint)
    }
}