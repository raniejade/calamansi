package calamansi.runtime

import calamansi.Node
import calamansi.Script
import calamansi.Component
import calamansi.event.Event
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import calamansi.runtime.scripting.ScriptLifeCycle
import calamansi.runtime.scripting.ScriptModule
import kotlin.reflect.KClass

class NodeImpl(private var _name: String, script: String?) : Node() {
    init {
        require(_name.isNotBlank())
    }

    private val scriptHandle = script?.let { Module.getModule<ScriptModule>().createScript(it, this) }
    private var _parent: NodeImpl? = null
    private val children = mutableSetOf<NodeImpl>()
    private val components = mutableMapOf<KClass<*>, Component>()
    // key: component, value: components (required by)
    private val requirements = mutableMapOf<KClass<*>, MutableSet<KClass<*>>>()

    override val script: Script?
        get() = scriptHandle?.let { Module.getModule<ScriptModule>().getScriptInstance(it) }

    var isAttached: Boolean = false
        set(value) {
            field = value

            for (child in children) {
                child.isAttached = field
            }
        }

    override fun <T : Component> addComponent(component: KClass<T>): T {
        check(components[component] == null) { "Node $this already contains component ${component.qualifiedName}" }
        val registryModule = Module.getModule<RegistryModule>()
        val qualifiedName = checkNotNull(component.qualifiedName)
        val requiredComponents = registryModule.getRequiredComponents(qualifiedName)
        val instance = registryModule.createComponentInstance(qualifiedName) as T
        if (requiredComponents.isNotEmpty()) {
            check(components.keys.containsAll(requiredComponents)) { "Missing required components for component '$qualifiedName'." }
            // establish relationship
            for (requiredComponent in requiredComponents) {
                val requirement = requirements.getOrPut(requiredComponent) { mutableSetOf() }
                requirement.add(component)
            }
        }
        components[component] = instance
        return instance
    }

    override fun <T : Component> getComponent(component: KClass<T>): T {
        return checkNotNull(components[component]) {
            "Node $this does contain component ${component.qualifiedName}"
        } as T
    }

    override fun <T : Component> hasComponent(component: KClass<T>): Boolean {
        return components.containsKey(component)
    }

    override fun <T : Component> removeComponent(component: KClass<T>): Boolean {
        // check if we can remove component
        val requirement = requirements.getOrElse(component) { mutableSetOf() }
        if (requirement.isNotEmpty()) {
            logger.info { "Can't remove component '${component.qualifiedName} as it is required by ${requirement}'" }
            return false
        }

        // break relationship
        val registryModule = Module.getModule<RegistryModule>()
        val qualifiedName = checkNotNull(component.qualifiedName)
        val requiredComponents = registryModule.getRequiredComponents(qualifiedName)
        for (requiredComponent in requiredComponents) {
            val requirement = requirements.getOrElse(requiredComponent) { mutableSetOf() }
            requirement.remove(component)
        }

        return components.remove(component) != null
    }

    override var name: String
        get() = _name
        set(value) {
            require(value.isNotBlank())
            // TODO: validation
            _name = value
        }
    override val parent: Node?
        get() = _parent

    override fun hasScript(): Boolean {
        return script != null
    }

    override suspend fun addChild(node: Node): Boolean {
        require(node is NodeImpl)
        if (isSelfOrAncestor(this, node)) {
            logger.warn { "Not adding $node to $this, cycles detected." }
            return false
        }
        // detach from current parent (if possible)
        detachFromParent(node)

        node._parent = this
        val added = children.add(node)

        node.isAttached = isAttached
        // only execute when node was successfully added
        if (added) {
            doExecuteAttachCallback(node)
        }

        return added
    }

    override suspend fun removeChild(node: Node): Boolean {
        require(node is NodeImpl)
        val removed = children.remove(node)

        // only execute when node is a child of this node
        if (removed) {
            doExecuteDetachCallback(node)
        }
        // detach callback can still see parent
        node._parent = null
        if (isAttached) {
            node.isAttached = false
        }

        return removed
    }

    override fun getChildren(): List<Node> {
        return children.toList()
    }

    override fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }

    suspend fun handleEvent(event: Event) {
        doExecuteHandleEventCallback(this, event)
    }

    suspend fun update(delta: Float) {
        doExecuteUpdateCallback(this, delta)
    }

    suspend fun attached() {
        doExecuteAttachCallback(this)
    }

    suspend fun detached() {
        doExecuteDetachCallback(this)
    }

    override fun toString(): String {
        return "Node(name=$_name)"
    }

    companion object {
        private val logger by lazy {
            Module.getLogger(NodeImpl::class)
        }

        private suspend fun detachFromParent(child: NodeImpl) {
            if (child._parent == null) {
                return
            }
            child._parent!!.removeChild(child)
        }

        private suspend fun doExecuteDetachCallback(node: NodeImpl) {
            if (!node.isAttached) {
                return
            }
            for (child in node.children) {
                doExecuteDetachCallback(child)
            }
            node.scriptHandle?.let {
                Module.getModule<ScriptModule>().invokeLifeCycle(it, ScriptLifeCycle.Detached)
            }
        }

        private suspend fun doExecuteAttachCallback(node: NodeImpl) {
            if (!node.isAttached) {
                return
            }
            node.scriptHandle?.let {
                Module.getModule<ScriptModule>().invokeLifeCycle(it, ScriptLifeCycle.Attached)
            }
            for (child in node.children) {
                doExecuteAttachCallback(child)
            }
        }

        private suspend fun doExecuteHandleEventCallback(node: NodeImpl, event: Event) {
            if (!node.isAttached) {
                return
            }
            node.scriptHandle?.let {
                Module.getModule<ScriptModule>().invokeLifeCycle(it, ScriptLifeCycle.HandleEvent(event))
            }

            // if event consumed we stop propagating
            if (event.isConsumed()) {
                return
            }

            for (child in node.children) {
                child.handleEvent(event)
            }
        }

        private suspend fun doExecuteUpdateCallback(node: NodeImpl, delta: Float) {
            if (!node.isAttached) {
                return
            }
            node.scriptHandle?.let {
                Module.getModule<ScriptModule>().invokeLifeCycle(it, ScriptLifeCycle.Update(delta))
            }
            for (child in node.children) {
                child.update(delta)
            }
        }

        private fun isSelfOrAncestor(node: NodeImpl?, potentialAncestor: NodeImpl): Boolean {
            if (node == null) {
                return false
            }
            return node === potentialAncestor || isSelfOrAncestor(node._parent, potentialAncestor)
        }
    }
}