package calamansi.runtime.resource

import calamansi.logging.Logger
import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.io.FileSystem
import kotlinx.serialization.modules.SerializersModule
import java.lang.ref.Cleaner
import java.lang.ref.WeakReference
import java.util.concurrent.ThreadFactory

class ResourceManager(
    module: SerializersModule,
    private val fs: FileSystem,
    private val logger: Logger
) {
    private val loaders = listOf<ResourceLoader<out Resource>>(
        SceneLoader()
    )

    private val json = Serializer(SerializersModule {
        include(module)
        contextual(ResourceRef::class, ResourceRefSerializer(this@ResourceManager))
    })

    private val cleaner = Cleaner.create(ThreadFactory {
        val thread = Thread(it)
        thread.name = "calamansi-resource-cleaner"
        thread.isDaemon = true
        return@ThreadFactory thread
    })

    private val loaderIndex: Map<String, ResourceLoader<out Resource>>
    private val resourceIndex = mutableMapOf<String, WeakReference<ResourceRef<out Resource>>>()

    init {
        val builder = mutableMapOf<String, ResourceLoader<out Resource>>()
        for (loader in loaders) {
            val supportedExtensions = loader.supportedExtensions
            for (extension in supportedExtensions) {
                if (builder.containsKey(extension)) {
                    logger.warn { "Ignoring extension: $extension as it is already used by ${builder[extension]}." }
                    continue
                }
                builder[extension] = loader
            }

        }
        loaderIndex = builder.toMap()
    }

    fun loadResource(resource: String): ResourceRef<out Resource> {
        val weakRef = resourceIndex[resource]
        if (weakRef != null) {
            val instance = weakRef.get()
            if (instance != null) {
                return instance
            }
            logger.warn { "Resource '$resource' has been garbage collected, reloading." }
        }

        logger.info { "Loading resource: '$resource'." }
        val loader = checkNotNull(loaderIndex[getExtension(resource)]) { "No loader found for resource: '$resource'." }
        val loadedResource = fs.getReader(resource).use {
            loader.load(it, json)
        }
        val resourceRef = ResourceRefImpl(resource, loadedResource.resource)
        cleaner.register(resourceRef, loadedResource.cleanup)
        return resourceRef
    }

    private class ResourceRefImpl<T : Resource>(override val path: String, val resource: T) : ResourceRef<T> {
        override fun get(): T {
            return resource
        }
    }

    companion object {
        private fun getExtension(resource: String): String {
            val extension = resource.substringAfterLast('.', "")
            check(extension.isNotBlank()) { "Unable to parse extension from '$resource'." }
            return extension
        }
    }
}