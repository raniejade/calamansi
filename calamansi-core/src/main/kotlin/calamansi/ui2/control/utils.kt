package calamansi.ui2.control

import calamansi.gfx.Color
import io.github.humbleui.skija.Paint

fun Color.toPaint(): Paint {
    return Paint().setARGB(a, r, g, b)
}

val Number.px: DimValue.Fixed
    get() = DimValue.Fixed(this.toFloat())

val Number.pc: DimValue.Percentage
    get() = DimValue.Percentage(this.toFloat())