package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property
import calamansi.runtime.utils.StateTracker
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Paint
import io.github.humbleui.skija.PaintMode
import io.github.humbleui.skija.TextBlob
import io.github.humbleui.skija.shaper.Shaper
import io.github.humbleui.types.Rect
import org.lwjgl.util.yoga.Yoga.*
import io.github.humbleui.skija.Font as SkijaFont

abstract class TextBase : CanvasElement() {
    internal var blob: TextBlob? = null

    abstract var text: String

    @Property
    var fontSize: Float = 12f

    @Property
    var fontColor: Color = Color.WHITE

    @Property
    lateinit var font: Font

    @Suppress("LeakingThis")
    private val fontState = StateTracker.create(
        this::font,
        this::fontSize
    )

    @Suppress("LeakingThis")
    private val textPaintState = StateTracker.create(
        this::fontColor
    )

    internal lateinit var skijaFont: SkijaFont
    private var textPaint: Paint = fontColor.toPaint()

    init {
        YGNodeSetMeasureFunc(ygNode) { _, width, widthMode, _, _, size ->
            blob?.close()
            blob = null
            if (text.isNotBlank()) {
                blob = Shaper.makePrimitive().use { shaper ->
                    when (widthMode) {
                        YGMeasureModeAtMost,
                        YGMeasureModeExactly -> {
                            shaper.shape(text, skijaFont, width)
                        }

                        YGMeasureModeUndefined -> {
                            shaper.shape(text, skijaFont)
                        }

                        else -> throw AssertionError("unsupported width mode: $widthMode")
                    }!!
                }
                size.width(blob!!.blockBounds.width)
                size.height(blob!!.blockBounds.height)
            } else {
                size.height(fontSize)
            }
        }
    }

    @Suppress("LeakingThis")
    private val textLayoutState = StateTracker.create(
        this::text,
        this::font,
        this::fontSize,
    )

    override fun layout() {
        super.layout()
        if (textLayoutState.isDirty()) {
            YGNodeMarkDirty(ygNode)
        }

        if (fontState.isDirty()) {
            skijaFont = font.fetchSkijaFont(fontSize)
        }
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

        val blob = blob
        if (blob != null) {

            val textXPos = getPaddingLeft() + getBorderLeft()
            val textYPos = getPaddingTop() + getBorderTop()
            // layout width/height less paddings and borders
            val textHeight =
                getLayoutHeight() - getPaddingTop() - getBorderTop() - getPaddingBottom() - getBorderBottom()
            val textWidth = getLayoutWidth() - getPaddingRight() - getBorderRight() - getPaddingLeft() - getBorderLeft()
            canvas.save()
            // debug clip rect
            canvas.drawRect(
                Rect.makeXYWH(
                    textXPos,
                    textYPos,
                    textWidth,
                    textHeight
                ),
                Color.BLUE.toPaint().setMode(PaintMode.STROKE)
            )
            canvas.clipRect(
                Rect.makeXYWH(
                    textXPos,
                    textYPos,
                    textWidth,
                    textHeight
                ),
            )

            canvas.drawTextBlob(
                blob,
                textXPos,
                textYPos,
                textPaint
            )
            canvas.restore()
            // debug text bounds
            canvas.drawRect(
                Rect.makeXYWH(
                    textXPos,
                    textYPos,
                    blob.blockBounds.width,
                    blob.blockBounds.height
                ),
                Color.RED.toPaint().setMode(PaintMode.STROKE)
            )
        }
    }
}