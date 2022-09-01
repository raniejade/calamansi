package calamansi.resource

interface ResourceRef<T : Resource> {
    val path: String
    suspend fun get(): T
}