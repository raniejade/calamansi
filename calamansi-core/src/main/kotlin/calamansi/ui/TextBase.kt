package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property
import calamansi.runtime.WindowContext
import calamansi.runtime.utils.StateTracker
import calamansi.runtime.utils.debugDraw
import io.github.humbleui.skija.*
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.paragraph.*
import io.github.humbleui.types.Rect
import org.lwjgl.util.yoga.Yoga.*
import io.github.humbleui.skija.Font as SkijaFont

abstract class TextBase : CanvasElement() {
    internal lateinit var pg: Paragraph

    abstract var text: String

    @Property
    var fontSize: Float by Theme.float()

    @Property
    var fontColor: Color by Theme.color()

    @Property
    var font: Font by Theme.font()

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
            if (::pg.isInitialized) {
                pg.close()
            }
            // use space to get bounds event if string is empty
            val text = text.ifEmpty { " " }
            val fc = FontCollection().setDefaultFontManager(FontMgr.getDefault())
            val ts = TextStyle().setForeground(textPaint)
                .setFontSize(fontSize)
                .setTypeface(skijaFont.typeface)
            pg = ParagraphBuilder(ParagraphStyle(), fc)
                .pushStyle(ts)
                .addText(text)
                .popStyle()
                .build()

            when (widthMode) {
                YGMeasureModeAtMost,
                YGMeasureModeExactly -> {
                    pg.layout(width)
                }

                YGMeasureModeUndefined -> {
                    pg.layout((executionContext as WindowContext).canvas.width.toFloat())
                }

                else -> throw AssertionError("unsupported width mode: $widthMode")
            }

            size.width(pg.longestLine)
            size.height(pg.height)
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

        if (textPaintState.isDirty()) {
            textPaint.close()
            textPaint = fontColor.toPaint()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val textXPos = getPaddingLeft() + getBorderLeft()
        val textYPos = getPaddingTop() + getBorderTop()
        // layout width/height less paddings and borders
        val textHeight = getAvailableHeight()
        val textWidth = getAvailableWidth()
        canvas.save()
        debugDraw {
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
        }
        canvas.clipRect(
            Rect.makeXYWH(
                textXPos,
                textYPos,
                textWidth,
                textHeight
            ),
        )
        pg.paint(canvas, textXPos, textYPos)
        canvas.restore()
        debugDraw {
            // debug text bounds
            canvas.drawRect(
                Rect.makeXYWH(
                    textXPos,
                    textYPos,
                    pg.longestLine,
                    pg.height
                ),
                Color.RED.toPaint().setMode(PaintMode.STROKE)
            )
        }
    }
}