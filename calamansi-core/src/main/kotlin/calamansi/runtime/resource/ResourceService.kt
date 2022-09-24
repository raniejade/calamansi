package calamansi.runtime.resource

import calamansi.resource.Resource
import calamansi.resource.ResourceLoader
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.source.FileSource
import calamansi.runtime.service.Service
import calamansi.runtime.service.Services
import calamansi.runtime.threading.CountingThreadFactory
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.ref.Cleaner
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.reflect.KClass

class ResourceService : Service {
    private val registryService: RegistryService by Services.get()
    private val sources = mutableMapOf<String, FileSource>()
    private val loaders = mutableMapOf<String, ResourceLoader>()
    private val locks = WeakHashMap<String, Semaphore>()
    private val logger = LoggerFactory.getLogger(ResourceService::class.java)
    private var json: Json? = null

    private val loadedResources = WeakHashMap<String, WeakReference<out Resource>>()
    private val cleaner = Cleaner.create(CountingThreadFactory("calamansi-resource-cleaner"))

    override fun start() {
        // resource loaders
        logger.info("Registering resource loaders.")
        val loaders = ServiceLoader.load(ResourceLoader::class.java)
        for (loader in loaders) {
            registerLoader(loader)
        }
    }

    fun registerSource(protocol: String, source: FileSource) {
        check(sources[protocol] == null) { "Protocol $protocol already in use by ${sources[protocol]}" }
        sources[protocol] = source
    }

    // not thread safe
    fun loadResource(resource: String, type: KClass<out Resource>, index: Int): Resource {
        logger.info("Loading resource: $resource.")
        val lock = getLock(resource)
        try {
            lock.acquire()
            val cached = loadedResources[resource]?.get()
            if (cached != null) {
                return cached
            }

            // not cached, reload (possibly gc'd)
            val parsedPath = ParsedPath.parse(resource)
            val source = getSource(parsedPath.protocol)
            val loader = getLoader(parsedPath.extension)
            val loadedResource = loader.loadResource(source.getReader(parsedPath.resource), type, index)
            loadedResource.resource._path = parsedPath.resource
            loadedResource.resource._index = index
            loadedResources[resource] = WeakReference(loadedResource.resource)
            cleaner.register(loadedResource.resource, loadedResource.free)
            return loadedResource.resource
        } finally {
            lock.release()
        }
    }

    private fun registerLoader(loader: ResourceLoader) {
        val supportedExtensions = loader.supportedExtensions
        for (extension in supportedExtensions) {
            if (loaders.containsKey(extension)) {
                logger.warn("Ignoring extension: $extension as it is already used by ${loaders[extension]}.")
                continue
            }
            loaders[extension] = loader
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

    private fun getLock(resource: String): Semaphore {
        return locks.getOrPut(resource) {
            Semaphore(1)
        }
    }

    private fun getSource(protocol: String): FileSource {
        return checkNotNull(sources[protocol]) {
            "Unable to find source for protocol $protocol."
        }
    }

    private fun getLoader(extension: String): ResourceLoader {
        return checkNotNull(loaders[extension]) {
            "Unable to find loader for extension $extension."
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
                val extension = resource.substringAfterLast("/", resource).substringAfter('.', "")
                check(extension.isNotBlank()) { "Unable to parse extension from '$resource'." }
                return extension
            }
        }
    }
}