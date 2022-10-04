package calamansi.ui

import calamansi.resource.Resource
import calamansi.runtime.gc.Bin
import io.github.humbleui.skija.Typeface
import io.github.humbleui.skija.Font as SkijaFont

class Font internal constructor(private val typeface: Typeface) : Resource() {
    private val cache = mutableMapOf<Float, SkijaFont>()

    init {
        val localCache = cache
        val localTypeface = typeface
        Bin.register(this) {
            localCache.forEach { (_, skijaFont) ->
                skijaFont.close()
            }
            localTypeface.close()
        }
    }

    internal fun fetchSkijaFont(size: Float): SkijaFont {
        return cache.getOrPut(size) {
            SkijaFont(typeface, size)
        }
    }
}