package calamansi.runtime.sys.window

import calamansi.event.Event
import calamansi.input.InputContext

interface Window : InputContext {
    var title: String

    fun show()

    fun registerEventHandler(handler: (Event) -> Unit): AutoCloseable
    fun pollEvents()

    fun closeWindow()
    fun shouldCloseWindow(): Boolean

    fun destroy()
}

interface WindowDriver {
    fun init()
    fun create(width: Int, height: Int, title: String = "", createContext: Boolean = false): Window
    fun shutdown()
}