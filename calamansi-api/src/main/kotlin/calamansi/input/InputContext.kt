package calamansi.input

interface InputContext {
    fun getKeyState(key: Key): InputState
    fun getMouseButtonState(button: MouseButton): InputState
}