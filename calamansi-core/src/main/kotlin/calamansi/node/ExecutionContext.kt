package calamansi.node

import calamansi.input.InputContext
import calamansi.resource.ResourceContext
import calamansi.resource.ResourceRef

interface ExecutionContext : InputContext, ResourceContext {
    fun setScene(ref: ResourceRef<Scene>)
    fun exit()
}