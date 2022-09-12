package calamansi.runtime.sys

import calamansi.event.Event
import calamansi.input.InputContext
import org.joml.Vector2fc
import org.joml.Vector2ic

fun interface WindowHandlerRegistration {
    fun unregister()
}

interface Window : InputContext {
    var title: String

    fun show()

    fun getContentScale(): Vector2fc
    fun getFramebufferSize(): Vector2ic

    fun registerEventHandler(handler: (Event) -> Unit): WindowHandlerRegistration
    fun registerPlatformStateChangeHandler(handler: (PlatformStateChange) -> Unit): WindowHandlerRegistration
    fun pollEvents()

    fun closeWindow()
    fun shouldCloseWindow(): Boolean

    fun destroy()
}

interface WindowDriver {
    fun start()
    fun create(width: Int, height: Int, title: String = "", createContext: Boolean = true): Window
    fun stop()
}