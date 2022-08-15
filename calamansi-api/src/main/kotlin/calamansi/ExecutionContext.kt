package calamansi

import calamansi.logging.LoggingContext
import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import kotlin.reflect.KClass

interface ExecutionContext : LoggingContext {
    val Script.owner: Node
    fun Node(name: String, script: KClass<out Script>? = null): Node
    fun setCurrentScene(scene: ResourceRef<Scene>)
    fun <T: Resource> loadResource(resource: String): ResourceRef<T>

    fun exit(exitCode: Int)
}