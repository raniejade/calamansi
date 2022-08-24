package calamansi.input

import calamansi.event.Event

sealed class InputEvent : Event()
class KeyStateEvent(val key: Key, val state: InputState, val modifiers: Set<InputModifier>) : InputEvent()
class MouseButtonStateEvent(val button: MouseButton, val state: InputState, val modifiers: Set<InputModifier>) :
    InputEvent()

class MouseMoveEvent(val x: Float, val y: Float) : InputEvent()
class TextEvent(val char: Char) : InputEvent()