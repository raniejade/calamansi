package calamansi.ui

import calamansi.gfx.Color
import calamansi.meta.Property
import calamansi.resource.Resource
import calamansi.runtime.utils.StateTracker
import org.jetbrains.skija.*
import java.util.Objects
import org.jetbrains.skija.Canvas as SkijaCanvas

sealed class StyledBox : Resource() {
    internal abstract fun draw(canvas: SkijaCanvas, x: Float, y: Float, width: Float, height: Float)
}

class EmptyStyledBox : StyledBox() {
    override fun draw(canvas: SkijaCanvas, x: Float, y: Float, width: Float, height: Float) {
        // nothing
    }

    override fun equals(other: Any?): Boolean {
        return other is EmptyStyledBox
    }
}

class FlatStyledBox : StyledBox() {
    @Property
    var backgroundColor: Color = Color.GREEN

    @Property
    var borderRadius = Corner()

    @Property
    var borderWidth = Box()

    @Property
    var borderColor = Color.TRANSPARENT

    private val backgroundPaintState = StateTracker.create(
        this::backgroundColor
    )

    private val borderPaintState = StateTracker.create(
        this::borderColor
    )

    private var backgroundPaint: Paint = backgroundColor.toPaint()

    private var borderPaint: Paint = borderColor.toPaint()

    override fun draw(canvas: SkijaCanvas, x: Float, y: Float, width: Float, height: Float) {
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
            borderRadius.topLeft,
            borderRadius.topRight,
            borderRadius.bottomRight,
            borderRadius.bottomLeft
        )

        val count = canvas.saveLayer(
            borderRect, null
        )
        if (borderWidth.left != 0f
            || borderWidth.top != 0f
            || borderWidth.right != 0f
            || borderWidth.bottom != 0f
        ) {
            canvas.drawRRect(
                borderRect,
                borderPaint
            )
        }

        canvas.drawRRect(
            RRect.makeXYWH(
                x + borderWidth.left,
                y + borderWidth.top,
                width - borderWidth.right - borderWidth.left,
                height - borderWidth.bottom - borderWidth.right,
                borderRadius.topLeft,
                borderRadius.topRight,
                borderRadius.bottomRight,
                borderRadius.bottomLeft
            ),
            backgroundPaint,
        )

        canvas.restoreToCount(count)
    }

    override fun equals(other: Any?): Boolean {
        if (other is FlatStyledBox) {
            return Objects.equals(backgroundColor, other.backgroundColor)
                    && Objects.equals(borderRadius, other.borderRadius)
                    && Objects.equals(borderWidth, other.borderWidth)
                    && Objects.equals(borderColor, other.borderColor)
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(
            backgroundColor,
            borderRadius,
            borderWidth,
            borderColor
        )
    }
}