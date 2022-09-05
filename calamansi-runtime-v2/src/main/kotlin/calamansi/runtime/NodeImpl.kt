package calamansi.runtime

import calamansi.*
import calamansi.component.Component
import calamansi.event.Event
import calamansi.meta.CalamansiInternal
import calamansi.runtime.logging.LoggingService
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.reflect.KClass

// placeholder
typealias Viewport = Any

@OptIn(CalamansiInternal::class)
open class NodeImpl(override var name: String, val script: Script?) : Node {
    protected val logger by lazy { Services.getService<LoggingService>().getLogger(this::class) }

    private val translation = Vector3f()
    private val rotation = Vector3f()
    private val scale = Vector3f()
    private val children = mutableSetOf<NodeImpl>()
    private val components = mutableMapOf<KClass<*>, Component>()

    init {
        script?._owner = this
        script?._logger = script?.let {
            Services.getService<LoggingService>().getLogger(it::class)
        }
    }

    var windowContext: WindowContext? = null
        set(value) {
            field = value

            children.forEach { it.windowContext = value }
        }

    override fun translate(translation: Vector3fc) {
        this.translation.add(translation)
    }

    override fun rotate(rotation: Vector3fc) {
        this.rotation.add(rotation)
    }

    override fun scale(scale: Vector3fc) {
        this.scale.add(scale)
    }

    override fun setTranslation(translation: Vector3fc) {
        this.translation.set(translation)
    }

    override fun setRotation(rotation: Vector3fc) {
        this.rotation.set(rotation)
    }

    override fun setScale(scale: Vector3fc) {
        this.scale.set(scale)
    }

    override fun getTranslation(): Vector3fc {
        return translation
    }

    override fun getRotation(): Vector3fc {
        return rotation
    }

    override fun getScale(): Vector3fc {
        return scale
    }

    override fun getTransform(): Matrix4f {
        TODO("Not yet implemented")
    }

    override fun getGlobalTransform(): Matrix4f {
        TODO("Not yet implemented")
    }

    override var parent: Node? = null
        set(value) {
            if (field != null) {
                if (isPartOfSceneTree()) {
                    invokeWithExecutionContext {
                        onDetached()
                        windowContext = null
                    }
                }
            }
            field = value
            if (isPartOfSceneTree()) {
                invokeWithExecutionContext {
                    windowContext = (parent as NodeImpl).windowContext
                    onAttached()
                }
            }
        }

    override fun addComponent(component: Component) {
        check(!components.containsKey(component::class)) { "Node already contains component: ${component::class.qualifiedName}" }
        components[component::class] = (component)
    }

    override fun <T : Component> removeComponent(type: KClass<T>) {
        checkNotNull(components.remove(type)) { "Node does not contain component: $type" }
    }

    override fun <T : Component> containsComponent(type: KClass<T>): Boolean {
        return components.containsKey(type)
    }

    override fun addChild(child: Node) {
        require(child is NodeImpl)
        check(!isSelfOrAncestor(this, child)) { "Not adding $child to $this, cycles detected." }
        check(children.add(child)) { "$child is already a child of $this" }
        child.parent = this
    }

    override fun removeChild(child: Node) {
        require(child is NodeImpl)
        check(children.remove(child)) { "$child is not a child of $this" }
        child.parent = null
    }

    override fun containsChild(child: Node) {
        require(child is NodeImpl)
        children.contains(child)
    }

    override fun getChildren(): List<Node> {
        return children.toList()
    }

    fun onAttached() {
        invokeWithExecutionContext {
            onAttached()
        }

        children.forEach(NodeImpl::onAttached)
    }

    fun onDetached() {
        children.forEach(NodeImpl::onDetached)
        invokeWithExecutionContext {
            onDetached()
        }
    }

    fun onUpdate(delta: Long) {
        invokeWithExecutionContext {
            onUpdate(delta)
        }

        children.forEach { it.onUpdate(delta) }
    }

    fun onEvent(event: Event) {
        invokeWithExecutionContext {
            onEvent(event)
        }

        if (event.isConsumed()) {
            return
        }

        children.forEach { it.onEvent(event) }
    }

    private fun invokeWithExecutionContext(cb: context(ExecutionContext) Script.() -> Unit) {
        if (script == null) {
            return
        }
        with(windowContext!!.getExecutionContext()) {
            cb(script)
        }
    }

    private fun isPartOfSceneTree(): Boolean {
        return windowContext != null || (parent as? NodeImpl)?.isPartOfSceneTree() ?: false
    }

    companion object {
        private fun isSelfOrAncestor(node: NodeImpl?, potentialAncestor: NodeImpl): Boolean {
            if (node == null) {
                return false
            }
            return node === potentialAncestor || isSelfOrAncestor(node.parent as NodeImpl, potentialAncestor)
        }

    }
}