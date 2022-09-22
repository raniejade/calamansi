package calamansi.runtime.font

import calamansi.resource.LoadedResource
import calamansi.resource.Resource
import calamansi.resource.ResourceLoader
import org.jetbrains.skija.Data
import org.jetbrains.skija.FontMgr
import java.io.InputStream
import kotlin.reflect.KClass

class FontLoader : ResourceLoader(setOf("ttf")) {
    override fun loadResource(stream: InputStream, type: KClass<out Resource>, index: Int): LoadedResource {
        val bytes = stream.readAllBytes()
        val typeface = Data.makeFromBytes(bytes).use { FontMgr.getDefault().makeFromData(it) }
        val font = FontImpl(typeface)
        return LoadedResource(font) {
            typeface.close()
        }
    }
}