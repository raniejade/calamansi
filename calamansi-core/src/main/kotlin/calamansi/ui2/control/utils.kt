package calamansi.ui2.control

import calamansi.gfx.Color
import io.github.humbleui.skija.Paint

fun Color.toPaint(): Paint {
    return Paint().setARGB(a, r, g, b)
}