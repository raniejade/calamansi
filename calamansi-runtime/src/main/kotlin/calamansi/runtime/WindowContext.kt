package calamansi.runtime

import calamansi.ExecutionContext
import calamansi.Node
import calamansi.Scene
import calamansi.Script
import calamansi.input.Key
import calamansi.input.MouseButton
import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.logging.LoggingService
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.ResourceService
import calamansi.runtime.sys.WindowHandlerRegistration
import calamansi.runtime.sys.Window
import kotlin.reflect.KClass

class WindowContext(private val window: Window) {
    private val logger by lazy { Services.getService<LoggingService>().getLogger(WindowContext::class) }
    private var root: NodeImpl? = null
    private var registration: WindowHandlerRegistration? = null
    private var executionContext = object : ExecutionContext {
        private val resourceService: ResourceService by Services.get()
        private val registryService: RegistryService by Services.get()

        override fun createNode(name: String, script: KClass<out Script>?): Node {
            val instance = script?.let {
                registryService.createScript(it.qualifiedName!!)
            }
            return NodeImpl(name, instance)
        }

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
    }

    fun getContentScale() = window.getContentScale()

    fun getFramebufferSize() = window.getFramebufferSize()

    fun pollEvents() {
        window.pollEvents()
    }

    fun shouldCloseWindow() = window.shouldCloseWindow()

    fun setScene(scene: ResourceRef<Scene>) {
        logger.info { "Switching to scene: ${scene.path}" }
        val newRoot = scene.get().instantiate() as NodeImpl?
        maybeDetachCurrentRoot()
        root = newRoot
        root?.windowContext = this
        root?.onAttached()
        // start receiving events
        registration = window.registerEventHandler {
            EventLoops.Script.scheduleNow {
                this.root?.onEvent(it)
            }
        }
    }

    fun onUpdate(delta: Long) {
        root?.onUpdate(delta)
    }

    fun getExecutionContext(): ExecutionContext {
        return executionContext
    }

    private fun maybeDetachCurrentRoot() {
        val localRoot = root ?: return
        // stop receiving events
        registration?.unregister()
        localRoot.onDetached()
        localRoot.windowContext = null
        registration = null
        root = null
    }
}