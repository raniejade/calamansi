package calamansi.runtime.resource

import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.EventLoops
import calamansi.runtime.Services
import calamansi.runtime.logging.LoggingService
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.loader.LoadedResource
import calamansi.runtime.resource.loader.ResourceLoader
import calamansi.runtime.resource.source.FileSource
import calamansi.runtime.service.Service
import calamansi.runtime.util.CountingThreadFactory
import kotlinx.serialization.json.Json
import java.io.IOException
import java.lang.ref.Cleaner
import java.lang.ref.WeakReference

class ResourceService : Service {
    private val registryService: RegistryService by Services.get()
    private val sources = mutableMapOf<String, FileSource>()
    private val loaders = mutableMapOf<String, ResourceLoader<out Resource>>()
    private val logger by lazy {
        Services.getService<LoggingService>().getLogger(ResourceService::class)
    }

    private var json: Json? = null

    private val loadedResources = mutableMapOf<String, WeakReference<ResourceRef<out Resource>>>()
    private val cleaner = Cleaner.create(CountingThreadFactory("calamansi-resource-cleaner"))

    override fun start() {
        // nada
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

    fun loadResource(resource: String, cb: ((ResourceRef<out Resource>) -> Unit)? = null): ResourceRef<out Resource> {
        logger.info { "Loading resource: $resource." }
        val cached = loadedResources[resource]?.get()
        if (cached != null) {
            cb?.invoke(cached)
            return cached
        }
        // not cached, reload (possibly gc'd)
        val path = ParsedPath.parse(resource)
        val source = getSource(path.protocol)
        val loader = getLoader(path.extension)
        return ResourceRefImpl(resource) {
            loader.load(source.getReader(path.resource))
        }
    }

    fun getJsonSerializer(): Json {
        val (rebuild, module) = registryService.getSerializersModule()
        if (rebuild || json == null) {
            json = Json {
                serializersModule = module
                encodeDefaults = false
                explicitNulls = false
            }
        }

        return json!!
    }

    override fun stop() {
        // nada
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

    private inner class ResourceRefImpl<T : Resource>(override val path: String, provider: () -> LoadedResource<T>) :
        ResourceRef<T> {
        private val future = EventLoops.Resource.schedule {
            val loadedResource = provider()
            // register for cleanup
            cleaner.register(this, loadedResource.cleanup)
            loadedResource.resource
        }

        override fun get(): T {
            return future.get()
        }

        override fun isLoaded(): Boolean {
            return future.isDone && !future.isCompletedExceptionally && !future.isCancelled
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