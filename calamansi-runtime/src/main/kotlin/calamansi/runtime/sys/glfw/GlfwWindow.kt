package calamansi.runtime.sys.glfw

import calamansi.event.Event
import calamansi.input.*
import calamansi.runtime.input.InputModifierMapper
import calamansi.runtime.input.InputStateMapper
import calamansi.runtime.input.KeyMapper
import calamansi.runtime.input.MouseButtonMapper
import calamansi.runtime.sys.Window
import calamansi.window.WindowCloseEvent
import calamansi.window.WindowFocusChangedEvent
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*

class GlfwWindow(private val window: Long, private val contextCreated: Boolean) : Window {
    private val eventHandlers = mutableSetOf<(Event) -> Unit>()

    init {
        // register input callbacks
        glfwSetKeyCallback(window, this::keyCallback)
        glfwSetCharCallback(window, this::charCallback)
        glfwSetMouseButtonCallback(window, this::mouseButtonCallback)
        glfwSetCursorPosCallback(window, this::mouseCursorPositionCallback)
        // register window callbacks
        glfwSetWindowFocusCallback(window, this::windowFocusCallback)
        glfwSetWindowCloseCallback(window, this::windowCloseCallback)
    }

    override var title: String = ""
        set(value) {
            field = value
            glfwSetWindowTitle(window, field)
        }

    override fun makeContextCurrent() {
        if (contextCreated) {
            glfwMakeContextCurrent(window)
        }
    }

    override fun swapBuffers() {
        if (contextCreated) {
            glfwSwapBuffers(window)
        }
    }

    override fun show() {
        glfwShowWindow(window)
    }

    override fun registerEventHandler(handler: (Event) -> Unit): AutoCloseable {
        eventHandlers.add(handler)
        return AutoCloseable { eventHandlers.remove(handler) }
    }

    override fun pollEvents() {
        glfwPollEvents()
    }

    override fun closeWindow() {
        glfwSetWindowShouldClose(window, true)
    }

    override fun shouldCloseWindow(): Boolean {
        return glfwWindowShouldClose(window)
    }

    override fun destroy() {
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
    }

    override fun getKeyState(key: Key): InputState {
        return InputStateMapper.fromGlfwState(glfwGetKey(window, KeyMapper.toGlfwKey(key)))
    }

    override fun getMouseButtonState(button: MouseButton): InputState {
        return InputStateMapper.fromGlfwState(glfwGetMouseButton(window, MouseButtonMapper.toGlfwButton(button)))
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