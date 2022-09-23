package calamansi.resource

import java.io.InputStream
import kotlin.reflect.KClass

data class LoadedResource(val resource: Resource, val free: () -> Unit)
abstract class ResourceLoader(val supportedExtensions: Set<String>) {
    abstract fun loadResource(stream: InputStream, type: KClass<out Resource>, index: Int): LoadedResource
}