package calamansi

import calamansi.input.InputContext
import calamansi.logging.LoggingContext
import calamansi.resource.Resource
import calamansi.resource.ResourceContext
import calamansi.resource.ResourceRef
import kotlin.reflect.KClass

interface ExecutionContext : LoggingContext, InputContext, ResourceContext {
    val Script.owner: Node
    fun Node(name: String, script: KClass<out Script>? = null): Node
    fun setCurrentScene(scene: ResourceRef<Scene>)

    fun exit(exitCode: Int)
}