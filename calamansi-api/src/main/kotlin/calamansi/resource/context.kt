package calamansi.resource

/**
 * Base type for all resources.
 */
interface Resource

/**
 * Reference to a [resource][Resource].
 */
interface ResourceRef<T : Resource> {
    val path: String

    /**
     * Fetch the underlying [resource][Resource].
     *
     * Blocks the current thread if loading is still in-progress.
     */
    fun get(): T

    /**
     * Check if the underlying [resource][Resource] is loaded.
     */
    fun isLoaded(): Boolean
}

interface ResourceContext {
    /**
     * Load a resource asynchronously.
     *
     * Optionally, a [callback][cb] can be passed which will be invoked when loading has completed.
     */
    fun <T : Resource> loadResource(path: String, cb: ((ResourceRef<T>) -> Unit)? = null): ResourceRef<T>
}