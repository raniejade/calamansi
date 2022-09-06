package calamansi.window

import calamansi.event.Event

sealed class WindowEvent : Event()

class WindowCloseEvent : WindowEvent()
data class WindowFocusChangedEvent(val focused: Boolean) : WindowEvent()