package calamansi.node

import calamansi.event.Event
import calamansi.internal.Lifecycle
import calamansi.meta.CalamansiInternal
import calamansi.meta.Property
import java.util.*

open class Node {
    private var onReadyInvoked = false
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
        onReadyInvoked = false
    }

    fun containsChild(node: Node): Boolean {
        return children.contains(node)
    }

    fun getChildren(): Set<Node> {
        return children.toSet()
    }

    context(ExecutionContext) protected open fun onReady() = Unit
    context(ExecutionContext) protected open fun onUpdate(delta: Long) = Unit
    context(ExecutionContext) protected open fun onEvent(event: Event) = Unit


    context(ExecutionContext) @OptIn(CalamansiInternal::class)
    internal fun invokeLifecycle(lifecycle: Lifecycle) {
        if (!onReadyInvoked) {
            onReady()
            onReadyInvoked = true
        }
        when (lifecycle) {
            is Lifecycle.OnEvent -> onEvent(lifecycle.event)
            is Lifecycle.OnUpdate -> onUpdate(lifecycle.delta)
        }
    }

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

