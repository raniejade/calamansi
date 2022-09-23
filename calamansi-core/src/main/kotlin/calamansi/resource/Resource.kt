package calamansi.resource

import kotlin.reflect.KClass

interface Resource

interface ResourceRef<T : Resource> {
    val path: String
    val type: KClass<T>
    val index: Int

    fun get(): T
}