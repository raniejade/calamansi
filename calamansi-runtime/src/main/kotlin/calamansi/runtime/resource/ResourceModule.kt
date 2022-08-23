package calamansi.runtime.resource

import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import calamansi.runtime.resource.source.FileSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.io.IOException
import java.lang.ref.Cleaner
import java.lang.ref.WeakReference
import java.util.concurrent.ThreadFactory

class ResourceModule : Module() {
    private val sources = mutableMapOf<String, FileSource>()
    private val loaders = mutableMapOf<String, ResourceLoader<out Resource>>()

    private val cleaner = Cleaner.create(ThreadFactory {
        val thread = Thread(it)
        thread.name = "calamansi-resource-cleaner"
        thread.isDaemon = true
        return@ThreadFactory thread
    })

    private val loadedResources = mutableMapOf<String, WeakReference<ResourceRef<out Resource>>>()

    override fun start() {
        logger.info { "Resource module started." }
    }

    fun registerSource(protocol: String, source: FileSource) {
        check(sources[protocol] == null) { "Protocol $protocol already in use by ${sources[protocol]}" }
        sources[protocol] = source
    }

    fun registerLoader(loader: ResourceLoader<out Resource>) {
        val supportedExtensions = loader.supportedExtensions
        for (extension in supportedExtensions) {
            if (loaders.containsKey(extension)) {
                logger.warn { "Ignoring extension: $extension as it is already used by ${loaders[extension]}." }
                continue
            }
            loaders[extension] = loader
        }

    }

    fun loadResource(resource: String): ResourceRef<out Resource> {
        val cached = loadedResources[resource]?.get()
        if (cached != null) {
            return cached
        }
        // not cached, reload (possibly gc'd)
        val path = ParsedPath.parse(resource)
        val source = getSource(path.protocol)
        val loader = getLoader(path.extension)
        val loadedResource = loader.load(source.getReader(path.resource))
        val resourceRef = ResourceRefImpl(resource, loadedResource.resource)
        // value is a weak reference, meaning it won't stop the ref from being gc'd
        loadedResources[resource] = WeakReference(resourceRef)
        // schedule cleanup when ref is gc'd
        cleaner.register(resourceRef, loadedResource.cleanup)
        return resourceRef
    }

    fun getJsonSerializer(): Json {
        return Json {
            encodeDefaults = false
            explicitNulls = false
            serializersModule = SerializersModule {
                include(getModule<RegistryModule>().getSerializersModule())
                contextual(ResourceRef::class, ResourceRefSerializer())
                registerJomlSerializers()
            }
        }
    }

    override fun shutdown() {
        logger.info { "Resource module shutting down." }
    }

    private fun getSource(protocol: String): FileSource {
        return checkNotNull(sources[protocol]) {
            "Unable to find source for protocol $protocol."
        }
    }

    private fun getLoader(extension: String): ResourceLoader<out Resource> {
        return checkNotNull(loaders[extension]) {
            "Unable to find loader for extension $extension."
        }
    }

    private class ResourceRefImpl<T : Resource>(override val path: String, val resource: T) : ResourceRef<T> {
        override fun get(): T {
            return resource
        }
    }

    private data class ParsedPath(val protocol: String, val resource: String, val extension: String) {
        companion object {
            private val regex = Regex("(?<source>\\w+):\\/\\/(?<path>.+)")
            fun parse(resource: String): ParsedPath {
                val match = regex.matchEntire(resource) ?: throw IOException("Unable to parse path: $resource")
                val source = checkNotNull(match.groups["source"]).value
                val path = checkNotNull(match.groups["path"]).value
                return ParsedPath(source, path, getExtension(path))
            }

            private fun getExtension(resource: String): String {
                val extension = resource.substringAfterLast('.', "")
                check(extension.isNotBlank()) { "Unable to parse extension from '$resource'." }
                return extension
            }
        }
    }
}