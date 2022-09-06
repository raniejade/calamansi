package calamansi.runtime.window.sys

import calamansi.event.Event
import calamansi.input.InputContext

fun interface EventHandlerRegistration {
    fun unregister()
}

interface Window : InputContext {
    var title: String

    fun show()

    fun registerEventHandler(handler: (Event) -> Unit): EventHandlerRegistration
    fun pollEvents()

    fun closeWindow()
    fun shouldCloseWindow(): Boolean

    fun destroy()
}

interface WindowDriver {
    fun start()
    fun create(width: Int, height: Int, title: String = "", createContext: Boolean = false): Window
    fun stop()
}