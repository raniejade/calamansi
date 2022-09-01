package calamansi

import calamansi.event.Event

abstract class Script {
    context(ExecutionContext) open suspend fun attached() = Unit
    context(ExecutionContext) open suspend fun detached() = Unit
    context(ExecutionContext) open suspend fun update(delta: Float) = Unit
    context(ExecutionContext) open suspend fun handleEvent(event: Event) = Unit

    final override fun hashCode(): Int {
        return super.hashCode()
    }

    final override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}