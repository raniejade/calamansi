package calamansi.ui2.control

import calamansi.gfx.Color
import calamansi.gfx.Font
import calamansi.runtime.utils.StateTracker
import calamansi.ui.toPaint
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.FontMgr
import io.github.humbleui.skija.Paint
import io.github.humbleui.skija.paragraph.*
import io.github.humbleui.skija.Font as SkijaFont

class Text(var text: String = "") : Control() {
    private lateinit var pg: Paragraph
    var fontColor: Color by Theme.color()
    var fontSize: Float by Theme.float()
    var font: Font by Theme.font()

    private val textState = StateTracker.create(
        this::text,
        this::fontColor,
        this::fontSize,
        this::font
    )

    private lateinit var skijaFont: SkijaFont
    private lateinit var skijaTextPaint: Paint

    override fun layout(width: Float, height: Float, forceLayout: Boolean) {
        super.layout(width, height, forceLayout)
        if (textState.isDirty() || forceLayout) {
            skijaFont = font.fetchSkijaFont(fontSize)
            skijaTextPaint = fontColor.toPaint()
            val fc = FontCollection()
            fc.setDefaultFontManager(FontMgr.getDefault())
            val ps = ParagraphStyle()
            val ts = TextStyle()
                .setTypeface(skijaFont.typeface)
                .setFontSize(fontSize)
                .setForeground(skijaTextPaint)
            val text = text.ifEmpty { " " }
            pg = ParagraphBuilder(ps, fc)
                .pushStyle(ts)
                .addText(text)
                .popStyle()
                .build()

            pg.layout(width)

            layoutWidth = pg.longestLine + paddingLeft + paddingRight + getBorderLeft() + getBorderRight()
            layoutHeight = pg.height + paddingTop + paddingBottom + getBorderTop() + getBorderBottom()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        // TODO: border
        val x = layoutX + paddingLeft + getBorderLeft()
        val y = layoutY + paddingTop + getBorderTop()
        canvas.translate(x, y)
        pg.paint(canvas, 0f, 0f)
        canvas.translate(-x, -y)
    }
}