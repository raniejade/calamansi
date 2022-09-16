package calamansi.node

import calamansi.event.Event
import calamansi.meta.Property
import java.util.*

open class Node {
    private var _id = UUID.randomUUID().toString()
    private var _parent: Node? = null
    private val children = mutableSetOf<Node>()

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
    }

    fun removeChild(node: Node) {
        check(children.remove(node)) { "$node not a child of $this." }
        node._parent = null
    }

    fun containsChild(node: Node): Boolean {
        return children.contains(node)
    }

    fun getChildren(): Set<Node> {
        return children.toSet()
    }

    context(ExecutionContext) open fun onInit() = Unit
    context(ExecutionContext) open fun onUpdate(delta: Long) = Unit
    context(ExecutionContext) open fun onEvent(event: Event) = Unit

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

