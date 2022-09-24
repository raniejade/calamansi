package calamansi.node

import calamansi.input.InputContext
import calamansi.resource.ResourceContext
import calamansi.ui.Canvas

interface ExecutionContext : InputContext, ResourceContext {
    val canvas: Canvas
    fun getFrameTime(): Float
    fun getFps(): Float
    fun setScene(scene: Scene)
    fun exit()
}