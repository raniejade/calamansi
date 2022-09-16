package calamansi.runtime

import calamansi.event.Event
import calamansi.input.Key
import calamansi.input.MouseButton
import calamansi.logging.Logger
import calamansi.node.ExecutionContext
import calamansi.node.Node
import calamansi.node.Scene
import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.logging.LoggingService
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.sys.Window
import calamansi.runtime.sys.WindowHandlerRegistration

class WindowContext(private val window: Window) {
    private val logger by lazy { Services.getService<LoggingService>().getLogger(WindowContext::class) }
    private var root: Node? = null
    private var registration: WindowHandlerRegistration? = null
    private var executionContext = object : ExecutionContext {
        private val resourceService: ResourceService by Services.get()
        private val registryService: RegistryService by Services.get()

        override fun setScene(scene: ResourceRef<Scene>) {
            this@WindowContext.setScene(scene)
        }

        override fun exit() {
            window.closeWindow()
        }

        override fun isKeyPressed(key: Key): Boolean {
            return window.isKeyPressed(key)
        }

        override fun isMouseButtonPressed(button: MouseButton): Boolean {
            return window.isMouseButtonPressed(button)
        }

        override fun <T : Resource> loadResource(path: String, cb: ((ResourceRef<T>) -> Unit)?): ResourceRef<T> {
            return resourceService.loadResource(path, cb as ((ResourceRef<out Resource>) -> Unit)?) as ResourceRef<T>
        }

        override inline val Node.logger: Logger
            get() {
                return Services.getService<LoggingService>().getLogger(this::class)
            }
    }

    fun getContentScale() = window.getContentScale()

    fun getFramebufferSize() = window.getFramebufferSize()

    fun pollEvents() {
        window.pollEvents()
    }

    fun shouldCloseWindow() = window.shouldCloseWindow()

    fun setScene(scene: ResourceRef<Scene>) {
        logger.info { "Switching to scene: ${scene.path}" }
        val newRoot = scene.get().instantiate()
        maybeDetachCurrentRoot()
        root = newRoot
        // start receiving events
        registration = window.registerEventHandler {
            EventLoops.Script.scheduleNow {
                invokeOnEvent(root, it)
            }
        }
    }

    fun onUpdate(delta: Long) {
        invokeOnUpdate(root, delta)
    }

    fun getExecutionContext(): ExecutionContext {
        return executionContext
    }

    private fun invokeOnUpdate(node: Node?, delta: Long) {
        if (node == null) {
            return
        }

        with(executionContext) {
            node.onUpdate(delta)
        }

        for (child in node.getChildren()) {
            invokeOnUpdate(child, delta)
        }
    }

    private fun invokeOnEvent(node: Node?, event: Event) {
        if (node == null) {
            return
        }

        with(executionContext) {
            node.onEvent(event)
        }

        if (event.isConsumed()) {
            return
        }

        for (child in node.getChildren()) {
            invokeOnEvent(child, event)
        }
    }

    private fun maybeDetachCurrentRoot() {
        // stop receiving events
        registration?.unregister()
        registration = null
        root = null
    }
}