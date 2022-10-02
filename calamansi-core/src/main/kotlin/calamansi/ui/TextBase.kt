package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property
import calamansi.runtime.utils.StateTracker
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Paint
import org.jetbrains.skija.TextBlob
import org.jetbrains.skija.shaper.Shaper
import org.lwjgl.util.yoga.Yoga.YGNodeMarkDirty
import org.lwjgl.util.yoga.Yoga.YGNodeSetMeasureFunc

abstract class TextBase(@Property var text: String) : CanvasElement() {
    private var blob: TextBlob? = null

    @Property
    var fontSize: Float = 12f

    @Property
    var fontColor: Color = Color.WHITE

    @Property
    lateinit var font: Font

    @Suppress("LeakingThis")
    private val textPaintState = StateTracker.create(
        this::fontColor
    )

    private var textPaint: Paint = fontColor.toPaint()

    @Suppress("LeakingThis")
    private val textLayoutState = StateTracker.create(
        this::text,
        this::font,
        this::fontSize,
        this::width,
    )

    override fun layout() {
        if (textLayoutState.isDirty()) {
            if (blob != null) {
                blob!!.close()
                blob = null
            }

            if (!text.isBlank()) {
                // TODO: how to support percentage based width and height?
                blob = Shaper.make().use { shaper ->
                    val width = width
                    val skijaFont = font.fetchSkijaFont(fontSize)
                    if (width is FlexValue.Fixed) {
                        shaper.shape(text, skijaFont, width.value)
                    } else {
                        shaper.shape(text, skijaFont)
                    }!!
                }

                val localBlob = blob!!
                YGNodeSetMeasureFunc(ygNode) { _, _, _, _, _, size ->
                    size.width(localBlob.blockBounds.width)
                    size.height(localBlob.blockBounds.height)
                }
                YGNodeMarkDirty(ygNode)
            }
        }

        super.layout()
    }

    override fun onThemeChanged(theme: Theme) {
        super.onThemeChanged(theme)
        font = theme.getFont(this::class, "font")
        fontSize = theme.getConstant(this::class, "fontSize")
        fontColor = theme.getColor(this::class, "fontColor")

    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (textPaintState.isDirty()) {
            textPaint.close()
            textPaint = fontColor.toPaint()
        }
        if (blob != null) {
            canvas.drawTextBlob(
                blob!!,
                getLayoutLeft() + getPaddingLeft() + getBorderLeft(),
                getLayoutTop() + getPaddingTop() + getBorderTop(),
                textPaint
            )
        }
    }
}