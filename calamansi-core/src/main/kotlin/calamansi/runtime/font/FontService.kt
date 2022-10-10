package calamansi.runtime.font

import calamansi.runtime.service.Service
import calamansi.gfx.Font
import io.github.humbleui.skija.Data
import io.github.humbleui.skija.FontMgr
import java.io.InputStream

class FontService : Service {
    override fun start() {
        // nada
    }

    fun createFont(inputStream: InputStream): Font {
        val typeface = Data.makeFromBytes(inputStream.readBytes()).use { FontMgr.getDefault().makeFromData(it) }
        return Font(typeface)
    }

    override fun stop() {
        // nada
    }
}