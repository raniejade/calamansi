package calamansi

import calamansi.event.Event

abstract class Script {
    context(ExecutionContext) open fun attached() = Unit
    context(ExecutionContext) open fun detached() = Unit
    context(ExecutionContext) open fun update(delta: Float) = Unit
    context(ExecutionContext) open fun handleEvent(event: Event) = Unit

    final override fun hashCode(): Int {
        return super.hashCode()
    }

    final override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}