package calamansi.runtime.window.sys.glfw

import calamansi.event.Event
import calamansi.input.*
import calamansi.runtime.window.sys.EventHandlerRegistration
import calamansi.runtime.window.sys.Window
import calamansi.window.WindowCloseEvent
import calamansi.window.WindowFocusChangedEvent
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*

class GlfwWindow(internal val handle: Long, private val contextCreated: Boolean) : Window {
    private val eventHandlers = mutableSetOf<(Event) -> Unit>()

    init {
        // register input callbacks
        glfwSetKeyCallback(handle, this::keyCallback)
        glfwSetCharCallback(handle, this::charCallback)
        glfwSetMouseButtonCallback(handle, this::mouseButtonCallback)
        glfwSetCursorPosCallback(handle, this::mouseCursorPositionCallback)
        // register window callbacks
        glfwSetWindowFocusCallback(handle, this::windowFocusCallback)
        glfwSetWindowCloseCallback(handle, this::windowCloseCallback)
    }

    override var title: String = ""
        set(value) {
            field = value
            glfwSetWindowTitle(handle, field)
        }

    fun makeContextCurrent() {
        if (contextCreated) {
            glfwMakeContextCurrent(handle)
        }
    }

    fun swapBuffers() {
        if (contextCreated) {
            glfwSwapBuffers(handle)
        }
    }

    override fun show() {
        glfwShowWindow(handle)
    }

    override fun registerEventHandler(handler: (Event) -> Unit): EventHandlerRegistration {
        eventHandlers.add(handler)
        return EventHandlerRegistration { eventHandlers.remove(handler) }
    }

    override fun pollEvents() {
        glfwPollEvents()
    }

    override fun closeWindow() {
        glfwSetWindowShouldClose(handle, true)
    }

    override fun shouldCloseWindow(): Boolean {
        return glfwWindowShouldClose(handle)
    }

    override fun destroy() {
        glfwFreeCallbacks(handle)
        glfwDestroyWindow(handle)
    }

    override fun isKeyPressed(key: Key): Boolean {
        return getKeyState(key) == InputState.PRESSED
    }

    override fun isMouseButtonPressed(button: MouseButton): Boolean {
        return getMouseButtonState(button) == InputState.PRESSED
    }

    private fun getKeyState(key: Key): InputState {
        return InputStateMapper.fromGlfwState(glfwGetKey(handle, KeyMapper.toGlfwKey(key)))
    }

    private fun getMouseButtonState(button: MouseButton): InputState {
        return InputStateMapper.fromGlfwState(glfwGetMouseButton(handle, MouseButtonMapper.toGlfwButton(button)))
    }

    private fun publishEvent(event: Event) {
        eventHandlers.forEach { it(event) }
    }

    private fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        publishEvent(
            KeyStateEvent(
                KeyMapper.fromGlfwKey(key),
                InputStateMapper.fromGlfwState(action),
                InputModifierMapper.fromGlfwModifier(mods)
            )
        )
    }

    private fun charCallback(window: Long, codePoint: Int) {
        val char = Char(codePoint)
        publishEvent(TextEvent(char))
    }

    private fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
        publishEvent(
            MouseButtonStateEvent(
                MouseButtonMapper.fromGlfwMouseButton(button),
                InputStateMapper.fromGlfwState(action),
                InputModifierMapper.fromGlfwModifier(mods)
            )
        )
    }

    private fun mouseCursorPositionCallback(window: Long, x: Double, y: Double) {
        publishEvent(MouseMoveEvent(x.toFloat(), y.toFloat()))
    }

    private fun windowCloseCallback(window: Long) {
        val event = WindowCloseEvent()
        publishEvent(event)
        if (!event.isConsumed()) {
            closeWindow()
        }
    }

    private fun windowFocusCallback(window: Long, focused: Boolean) {
        publishEvent(WindowFocusChangedEvent(focused))
    }
}