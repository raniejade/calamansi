package calamansi.resource

import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass


interface ResourceContext {
    fun <T : Resource> loadResource(path: String, type: KClass<T>, index: Int = 0): ResourceRef<T> {
        return loadResourceAsync(path, type, index).get()
    }

    fun <T : Resource> loadResourceAsync(
        path: String,
        type: KClass<T>,
        index: Int = 0,
    ): CompletableFuture<ResourceRef<T>>
}

inline fun <reified T : Resource> ResourceContext.loadResource(path: String, index: Int = 0) =
    loadResource(path, T::class, index)

inline fun <reified T : Resource> ResourceContext.loadResourceAsync(path: String, index: Int = 0) =
    loadResourceAsync(path, T::class, index)