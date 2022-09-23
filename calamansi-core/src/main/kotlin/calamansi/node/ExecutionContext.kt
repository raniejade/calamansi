package calamansi.node

import calamansi.input.InputContext
import calamansi.resource.ResourceContext
import calamansi.resource.ResourceRef
import calamansi.ui.Canvas
import calamansi.ui.Font

interface ExecutionContext : InputContext, ResourceContext {
    val canvas: Canvas
    fun getFrameTime(): Float
    fun getFps(): Float
    fun setScene(ref: ResourceRef<Scene>)
    fun exit()
}