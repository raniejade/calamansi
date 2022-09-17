package calamansi.resource

import kotlin.reflect.KClass

/**
 * Reference to a [resource][Resource].
 */
interface ResourceRef<T : Resource> {
    val path: String
    val type: KClass<T>
    val index: Int

    /**
     * Fetch the underlying [resource][Resource].
     */
    fun get(): T
}