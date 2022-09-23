package calamansi.node

import calamansi.input.InputContext
import calamansi.resource.ResourceRef

internal interface ExecutionContext : InputContext {
    fun setScene(ref: ResourceRef<Scene>)
    fun exit()
}