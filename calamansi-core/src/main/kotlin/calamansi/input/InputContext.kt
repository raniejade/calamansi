package calamansi.input

interface InputContext {
    fun isKeyPressed(key: Key): Boolean
    fun isMouseButtonPressed(button: MouseButton): Boolean
}