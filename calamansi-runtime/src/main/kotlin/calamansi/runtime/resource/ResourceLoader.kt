package calamansi.runtime.resource

import calamansi.resource.Resource
import java.io.InputStream

data class LoadedResource<T : Resource>(val resource: T, val cleanup: () -> Unit)

interface ResourceLoader<T : Resource> {
    val supportedExtensions: Set<String>
    fun load(inputStream: InputStream): LoadedResource<T>
}