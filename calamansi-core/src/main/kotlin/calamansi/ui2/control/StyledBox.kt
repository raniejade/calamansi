package calamansi.ui2.control

import calamansi.gfx.Color
import calamansi.resource.Resource
import calamansi.runtime.utils.StateTracker
import io.github.humbleui.skija.BlendMode
import io.github.humbleui.skija.Canvas
import io.github.humbleui.skija.Paint
import io.github.humbleui.types.RRect
import java.util.*

sealed class StyledBox : Resource() {
    internal abstract fun draw(canvas: Canvas, x: Float, y: Float, width: Float, height: Float)
}

class EmptyStyledBox : StyledBox() {
    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        // nothing
    }

    override fun equals(other: Any?): Boolean {
        return other is EmptyStyledBox
    }
}

class FlatStyledBox : StyledBox() {
    var backgroundColor: Color = Color.TRANSPARENT

    var borderTopLeftRadius: Float = 0f
    var borderTopRightRadius: Float = 0f
    var borderBottomLeftRadius: Float = 0f
    var borderBottomRightRadius: Float = 0f

    var borderLeftWidth: Float = 0f
    var borderTopWidth: Float = 0f
    var borderRightWidth: Float = 0f
    var borderBottomWidth: Float = 0f

    var borderColor = Color.TRANSPARENT

    fun setBorderWidth(width: Float) {
        borderLeftWidth = width
        borderTopWidth = width
        borderRightWidth = width
        borderBottomWidth = width
    }

    fun setBorderRadius(radius: Float) {
        borderTopLeftRadius = radius
        borderTopRightRadius = radius
        borderBottomLeftRadius = radius
        borderBottomRightRadius = radius
    }

    private val backgroundPaintState = StateTracker.create(
        this::backgroundColor
    )

    private val borderPaintState = StateTracker.create(
        this::borderColor
    )

    private var backgroundPaint: Paint = backgroundColor.toPaint()

    private var borderPaint: Paint = borderColor.toPaint()

    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        if (backgroundPaintState.isDirty()) {
            backgroundPaint.close()
            backgroundPaint = backgroundColor.toPaint().setBlendMode(BlendMode.SRC)
        }

        if (borderPaintState.isDirty()) {
            borderPaint.close()
            borderPaint = borderColor.toPaint()
        }

        val borderRect = RRect.makeXYWH(
            x,
            y,
            width,
            height,
            borderTopLeftRadius,
            borderTopRightRadius,
            borderBottomRightRadius,
            borderBottomLeftRadius
        )

        val count = canvas.saveLayer(
            borderRect, null
        )
        if (borderLeftWidth != 0f
            || borderTopWidth != 0f
            || borderRightWidth != 0f
            || borderBottomWidth != 0f
        ) {
            canvas.drawRRect(
                borderRect,
                borderPaint
            )
        }

        canvas.drawRRect(
            RRect.makeXYWH(
                x + borderLeftWidth,
                y + borderTopWidth,
                width - borderRightWidth - borderLeftWidth,
                height - borderBottomWidth - borderTopWidth,
                borderTopLeftRadius,
                borderTopRightRadius,
                borderBottomRightRadius,
                borderBottomLeftRadius
            ),
            backgroundPaint,
        )

        canvas.restoreToCount(count)
    }

    override fun equals(other: Any?): Boolean {
        if (other is FlatStyledBox) {
            return Objects.equals(backgroundColor, other.backgroundColor)
                    && Objects.equals(borderTopLeftRadius, other.borderTopLeftRadius)
                    && Objects.equals(borderTopRightRadius, other.borderTopRightRadius)
                    && Objects.equals(borderBottomRightRadius, other.borderBottomRightRadius)
                    && Objects.equals(borderBottomLeftRadius, other.borderBottomLeftRadius)
                    && Objects.equals(borderLeftWidth, other.borderLeftWidth)
                    && Objects.equals(borderTopWidth, other.borderTopWidth)
                    && Objects.equals(borderRightWidth, other.borderRightWidth)
                    && Objects.equals(borderBottomWidth, other.borderBottomWidth)
                    && Objects.equals(borderColor, other.borderColor)
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(
            backgroundColor,
            borderTopLeftRadius,
            borderTopRightRadius,
            borderBottomRightRadius,
            borderBottomLeftRadius,
            borderLeftWidth,
            borderTopWidth,
            borderRightWidth,
            borderBottomWidth,
            borderColor
        )
    }
}