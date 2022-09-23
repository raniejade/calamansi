package calamansi.ui

import calamansi.resource.Resource
import calamansi.runtime.gc.Bin
import org.jetbrains.skija.Font
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.Font as SkijaFont

class Font internal constructor(private val typeface: Typeface) : Resource {
    private val cache = mutableMapOf<Float, SkijaFont>()

    init {
        Bin.register(this) {
            cache.forEach { (_, skijaFont) ->
                skijaFont.close()
            }
            typeface.close()
        }
    }

    internal fun makeSkijaFont(size: Float): SkijaFont {
        return cache.getOrPut(size) {
            Font(typeface, size)
        }
    }
}