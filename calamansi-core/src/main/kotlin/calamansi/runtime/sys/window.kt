package calamansi.runtime.sys

import calamansi.event.Event
import calamansi.input.InputContext
import calamansi.ui.Cursor
import org.joml.Vector2fc
import org.joml.Vector2ic

internal fun interface WindowHandlerRegistration {
    fun unregister()
}

internal interface Window : InputContext {
    var title: String

    fun show()

    fun getWindowSize(): Vector2ic
    fun getContentScale(): Vector2fc
    fun getFramebufferSize(): Vector2ic

    fun registerEventHandler(handler: (Event) -> Unit): WindowHandlerRegistration
    fun registerPlatformStateChangeHandler(handler: (PlatformStateChange) -> Unit): WindowHandlerRegistration
    fun pollEvents()
    fun processEvents()

    fun setCursor(cursor: Cursor)

    fun closeWindow()
    fun shouldCloseWindow(): Boolean

    fun destroy()
}

internal interface WindowDriver {
    fun start()
    fun create(width: Int, height: Int, title: String = "", createContext: Boolean = true): Window
    fun stop()
}