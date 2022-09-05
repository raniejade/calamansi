package calamansi

import calamansi.input.InputContext
import calamansi.logging.LoggingContext
import calamansi.resource.ResourceContext
import calamansi.resource.ResourceRef
import kotlin.reflect.KClass

interface ExecutionContext : InputContext, ResourceContext {
    fun createNode(name: String, script: KClass<out Script>? = null): Node
    fun setScene(scene: ResourceRef<Scene>)

    fun exit()
}