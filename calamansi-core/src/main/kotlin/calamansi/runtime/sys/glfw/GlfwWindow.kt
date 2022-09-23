package calamansi.runtime.sys.glfw

import calamansi.event.Event
import calamansi.input.*
import calamansi.runtime.sys.PlatformStateChange
import calamansi.runtime.sys.Window
import calamansi.runtime.sys.WindowHandlerRegistration
import calamansi.window.WindowCloseEvent
import calamansi.window.WindowFocusChangedEvent
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack.stackPush

internal class GlfwWindow(val handle: Long, private val contextCreated: Boolean) : Window {
    private val eventHandlers = mutableSetOf<(Event) -> Unit>()
    private val platformStateChangeHandlers = mutableSetOf<(PlatformStateChange) -> Unit>()

    init {
        // register input callbacks
        glfwSetKeyCallback(handle, this::keyCallback)
        glfwSetCharCallback(handle, this::charCallback)
        glfwSetMouseButtonCallback(handle, this::mouseButtonCallback)
        glfwSetCursorPosCallback(handle, this::mouseCursorPositionCallback)
        // register window callbacks
        glfwSetWindowFocusCallback(handle, this::windowFocusCallback)
        glfwSetWindowCloseCallback(handle, this::windowCloseCallback)
        glfwSetWindowSizeCallback(handle, this::windowResizedCallback)
        glfwSetFramebufferSizeCallback(handle, this::framebufferResizedCallback)
    }

    override var title: String = ""
        set(value) {
            field = value
            glfwSetWindowTitle(handle, field)
        }

    fun swapBuffers() {
        if (contextCreated) {
            glfwSwapBuffers(handle)
        }
    }

    override fun show() {
        glfwShowWindow(handle)
    }

    override fun getWindowSize(): Vector2ic {
        return stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            glfwGetWindowSize(handle, width, height)
            Vector2i(width[0], height[0])
        }
    }

    override fun getContentScale(): Vector2fc {
        return stackPush().use { stack ->
            val scaleX = stack.mallocFloat(1)
            val scaleY = stack.mallocFloat(1)
            glfwGetWindowContentScale(handle, scaleX, scaleY)
            Vector2f(scaleX[0], scaleY[0])
        }
    }

    override fun getFramebufferSize(): Vector2ic {
        return stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            glfwGetFramebufferSize(handle, width, height)
            Vector2i(width[0], height[0])
        }
    }

    override fun registerEventHandler(handler: (Event) -> Unit): WindowHandlerRegistration {
        eventHandlers.add(handler)
        return WindowHandlerRegistration { eventHandlers.remove(handler) }
    }

    override fun registerPlatformStateChangeHandler(handler: (PlatformStateChange) -> Unit): WindowHandlerRegistration {
        platformStateChangeHandlers.add(handler)
        return WindowHandlerRegistration {
            platformStateChangeHandlers.remove(handler)
        }
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

    private fun publishPlatformStateChange(stateChange: PlatformStateChange) {
        platformStateChangeHandlers.forEach { it(stateChange) }
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

    private fun windowResizedCallback(window: Long, width: Int, height: Int) {
        publishPlatformStateChange(PlatformStateChange.WindowSize(width, height))
    }

    private fun framebufferResizedCallback(window: Long, width: Int, height: Int) {
        publishPlatformStateChange(PlatformStateChange.FramebufferSize(width, height))
    }
}