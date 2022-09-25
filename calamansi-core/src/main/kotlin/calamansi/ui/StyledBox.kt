package calamansi.ui

import calamansi.gfx.Color
import calamansi.resource.Resource
import org.jetbrains.skija.Paint
import org.jetbrains.skija.RRect
import org.joml.Vector4f
import org.jetbrains.skija.Canvas as SkijaCanvas

sealed class StyledBox : Resource() {
    internal abstract fun draw(canvas: SkijaCanvas, x: Float, y: Float, width: Float, height: Float)
}

class EmptyStyledBox : StyledBox() {
    override fun draw(canvas: SkijaCanvas, x: Float, y: Float, width: Float, height: Float) {
        // nothing
    }
}

class FlatStyledBox : StyledBox() {
    var backgroundColor: Color = Color.GREEN
        set(value) {
            field = value
            backgroundPaint = value.toPaint()
        }

    var borderRadius = Vector4f()

    private var backgroundPaint: Paint = backgroundColor.toPaint()

    override fun draw(canvas: SkijaCanvas, x: Float, y: Float, width: Float, height: Float) {
        canvas.drawRRect(
            RRect.makeXYWH(x, y, width, height, borderRadius.x, borderRadius.y, borderRadius.z, borderRadius.w),
            backgroundPaint,
        )
    }
}