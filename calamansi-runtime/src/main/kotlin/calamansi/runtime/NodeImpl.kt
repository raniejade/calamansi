package calamansi.runtime

import calamansi.ExecutionContext
import calamansi.Node
import calamansi.Script
import calamansi.component.Component
import kotlin.reflect.KClass

class NodeImpl(private var _name: String, private val componentManager: ComponentManager, override val script: Script?) : Node() {
    init {
        require(_name.isNotBlank())
    }
    private var _parent: NodeImpl? = null
    private val children = mutableSetOf<NodeImpl>()
    private val components = mutableMapOf<KClass<*>, Component>()

    var isSceneRoot: Boolean = false
        get() = field
        set(value) {
            require(_parent == null) { "isSceneRoot can only be set when _parent is null" }
            field = value
        }

    context(ExecutionContext) override fun <T : Component> addComponent(component: KClass<T>): T {
        check(components[component] == null) { "Node $this already contains component ${component.qualifiedName}" }
        val instance = componentManager.createComponent(component) as T
        components[component] = instance
        return instance
    }

    context(ExecutionContext) override fun <T : Component> getComponent(component: KClass<T>): T {
        return checkNotNull(components[component]) {
            "Node $this does contain component ${component.qualifiedName}"
        } as T
    }

    context(ExecutionContext) override fun <T : Component> hasComponent(component: KClass<T>): Boolean {
        return components.containsKey(component)
    }

    context(ExecutionContext) override fun <T : Component> removeComponent(component: KClass<T>): Boolean {
        return components.remove(component) != null
    }

    context(ExecutionContext) override var name: String
        get() = _name
        set(value) {
            require(value.isNotBlank())
            // TODO: validation
            _name = value
        }
    context(ExecutionContext) override val parent: Node?
        get() = _parent

    context(ExecutionContext) override fun hasScript(): Boolean {
        return script != null
    }

    context(ExecutionContext) override fun addChild(node: Node): Boolean {
        require(node is NodeImpl)
        if (isSelfOrAncestor(this, node)) {
            logger.warn { "Not adding $node to $this, cycles detected." }
            return false
        }
        // detach from current parent (if possible)
        detachFromParent(node)

        node._parent = this
        val added = children.add(node)

        // only execute when node was successfully added
        if (added) {
            doExecuteAttachCallback(node)
        }

        return added
    }

    context(ExecutionContext) override fun removeChild(node: Node): Boolean {
        require(node is NodeImpl)
        val removed = children.remove(node)

        // only execute when node is a child of this node
        if (removed) {
            doExecuteDetachCallback(node)
        }
        // detach callback can still see parent
        node._parent = null

        return removed
    }

    context(ExecutionContext) override fun getChildren(): List<Node> {
        return children.toList()
    }

    context(ExecutionContext) fun executeUpdateCallback(delta: Float) {
        doExecuteUpdateCallback(this, delta)
    }

    context(ExecutionContext) fun executeAttachCallback() {
        doExecuteAttachCallback(this)
    }

    context(ExecutionContext) fun executeDetachCallback() {
        doExecuteDetachCallback(this)
    }

    private fun isPartOfCurrentScene(): Boolean {
        if (_parent == null) {
            return isSceneRoot
        }

        return checkNotNull(_parent).isPartOfCurrentScene()
    }

    override fun toString(): String {
        return "Node(name=$_name)"
    }

    companion object {
        context(ExecutionContext) private fun detachFromParent(child: NodeImpl) {
            if (child._parent == null) {
                return
            }
            child._parent!!.removeChild(child)
        }

        context(ExecutionContext) private fun doExecuteDetachCallback(node: NodeImpl) {
            if (!node.isPartOfCurrentScene()) {
                return
            }
            for (child in node.children) {
                doExecuteDetachCallback(child)
            }
            node.script?.detached()
        }

        context(ExecutionContext) private fun doExecuteAttachCallback(node: NodeImpl) {
            if (!node.isPartOfCurrentScene()) {
                return
            }
            node.script?.attached()
            for (child in node.children) {
                doExecuteAttachCallback(child)
            }
        }

        context(ExecutionContext) private fun doExecuteUpdateCallback(node: NodeImpl, delta: Float) {
            if (!node.isPartOfCurrentScene()) {
                return
            }
            node.script?.update(delta)
            for (child in node.children) {
                child.executeUpdateCallback(delta)
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