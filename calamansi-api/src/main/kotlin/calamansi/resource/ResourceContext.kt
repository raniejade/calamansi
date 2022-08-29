package calamansi.resource

interface ResourceContext {
    fun <T: Resource> loadResource(resource: String): ResourceRef<T>
}