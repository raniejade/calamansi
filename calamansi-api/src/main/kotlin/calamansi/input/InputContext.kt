package calamansi.input

interface InputContext {
    fun getKeyState(key: Key): InputState
    fun getMouseButtonState(button: MouseButton): InputState

    fun isKeyPressed(key: Key): Boolean {
        return getKeyState(key) == InputState.PRESSED
    }

    fun isMouseButtonPressed(button: MouseButton): Boolean {
        return getMouseButtonState(button) == InputState.PRESSED
    }
}