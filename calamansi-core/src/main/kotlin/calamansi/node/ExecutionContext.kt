package calamansi.node

import calamansi.input.InputContext
import calamansi.resource.ResourceContext
import calamansi.ui.Canvas
import calamansi.ui.Cursor
import calamansi.ui.Theme

interface ExecutionContext : InputContext, ResourceContext {
    fun setTheme(theme: Theme)
    val canvas: Canvas
    fun getFrameTime(): Float
    fun getFps(): Float
    fun setScene(scene: Scene)
    fun exit()
    fun setCursor(cursor: Cursor)
}