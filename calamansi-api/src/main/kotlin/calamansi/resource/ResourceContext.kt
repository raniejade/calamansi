package calamansi.resource

interface ResourceContext {
    suspend fun <T : Resource> fetchResource(resource: String, preload: Boolean = true): ResourceRef<T>
}