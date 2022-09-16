package calamansi.node

import calamansi.input.InputContext
import calamansi.logging.LoggingContext
import calamansi.resource.ResourceContext
import calamansi.resource.ResourceRef

interface ExecutionContext : InputContext, ResourceContext, LoggingContext {
    fun setScene(scene: ResourceRef<Scene>)
    fun exit()
}