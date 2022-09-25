package calamansi.node

import calamansi.event.Event
import calamansi.meta.Property
import java.util.*

open class Node {
    private var _id = UUID.randomUUID().toString()
    private var _parent: Node? = null
        set(value) {
            val oldParent = field
            field = value
            parentChanged(oldParent, field)
        }

    private val children = mutableSetOf<Node>()

    internal var executionContext: ExecutionContext? = null
        set(value) {
            field = value
            getChildren().forEach { it.executionContext = value }
        }

    @Property
    var name: String = "${this::class.simpleName}"

    var parent: Node?
        get() = _parent
        set(value) {
            value?.addChild(this)

        }

    fun addChild(node: Node) {
        check(!children.contains(node))
        // remove previous parent
        node._parent?.removeChild(node)
        node._parent = this
        children.add(node)
        node.executionContext = executionContext
        if (executionContext != null) {
            // part of the active scene
            node.invokeOnEnterTree()
        }
        childAdded(node)
    }

    fun removeChild(node: Node) {
        check(children.remove(node)) { "$node not a child of $this." }
        node._parent = null
        if (executionContext != null) {
            node.invokeOnExitTree()
        }
        node.executionContext = null
        childRemoved(node)
    }

    fun containsChild(node: Node): Boolean {
        return children.contains(node)
    }

    fun getChildren(): Set<Node> {
        return children.toSet()
    }

    context (ExecutionContext) open fun onEnterTree() = Unit
    context (ExecutionContext) open fun onEvent(event: Event) = Unit
    context (ExecutionContext) open fun onUpdate(delta: Float) = Unit
    context (ExecutionContext) open fun onExitTree() = Unit

    internal fun invokeOnEnterTree() {
        // make sure children are ready
        for (child in children) {
            child.invokeOnEnterTree()
        }

        nodeEnterTree()
        with(checkNotNull(executionContext)) {
            onEnterTree()
        }
    }

    internal fun invokeOnExitTree() {
        // make sure children are ready
        for (child in children) {
            child.invokeOnExitTree()
        }

        nodeExitTree()
        with(checkNotNull(executionContext)) {
            onExitTree()
        }
    }

    internal fun invokeOnEvent(event: Event) {
        with(checkNotNull(executionContext)) {
            onEvent(event)
        }
        if (event.isConsumed()) {
            return
        }

        for (child in children) {
            child.invokeOnEvent(event)
        }
    }

    internal fun invokeOnUpdate(delta: Float) {
        with(checkNotNull(executionContext)) {
            onUpdate(delta)
        }

        for (child in children) {
            child.invokeOnUpdate(delta)
        }
    }

    internal open fun nodeEnterTree() = Unit
    internal open fun nodeExitTree() = Unit
    internal open fun childAdded(child: Node) = Unit
    internal open fun childRemoved(child: Node) = Unit
    internal open fun parentChanged(old: Node?, new: Node?) = Unit

    final override fun hashCode(): Int {
        return _id.hashCode()
    }

    final override fun equals(other: Any?): Boolean {
        if (other !is Node) {
            return false
        }
        return other._id == _id
    }
}