package calamansi.input

import calamansi.event.Event

sealed class InputEvent : Event()
data class KeyStateEvent(val key: Key, val state: InputState, val modifiers: Set<InputModifier>) : InputEvent()
data class MouseButtonStateEvent(val button: MouseButton, val state: InputState, val modifiers: Set<InputModifier>) :
    InputEvent()

data class MouseMoveEvent(val x: Float, val y: Float) : InputEvent()
data class TextEvent(val char: Char) : InputEvent()