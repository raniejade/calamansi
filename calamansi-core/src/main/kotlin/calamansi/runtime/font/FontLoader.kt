package calamansi.runtime.font

import calamansi.resource.LoadedResource
import calamansi.resource.Resource
import calamansi.resource.ResourceLoader
import calamansi.runtime.service.Services
import java.io.InputStream
import kotlin.reflect.KClass

class FontLoader : ResourceLoader(setOf("ttf")) {
    private val fontService: FontService by Services.get()

    override fun loadResource(stream: InputStream, type: KClass<out Resource>, index: Int): LoadedResource {
        val font = fontService.createFont(stream)
        return LoadedResource(font) {
            // Font class itself handles cleanup
        }
    }
}