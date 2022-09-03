package calamansi

import calamansi.input.InputContext
import calamansi.resource.ResourceContext
import calamansi.resource.ResourceRef
import kotlin.reflect.KClass

interface ExecutionContext : InputContext, ResourceContext {
    val Script.owner: Node

    fun createNode(name: String, script: KClass<out Script>? = null): Node
    fun createGroup(name: String, script: KClass<out Script>? = null): Group
    fun setScene(scene: ResourceRef<Scene>)
}