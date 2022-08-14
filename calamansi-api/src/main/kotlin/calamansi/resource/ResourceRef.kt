package calamansi.resource

interface ResourceRef<T : Resource> {
    val path: String
    fun get(): T
}