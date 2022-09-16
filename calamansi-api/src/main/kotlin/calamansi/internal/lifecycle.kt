package calamansi.internal

import calamansi.event.Event
import calamansi.meta.CalamansiInternal
import calamansi.node.ExecutionContext
import calamansi.node.Node

@CalamansiInternal
sealed class Lifecycle {
    class OnUpdate(val delta: Long) : Lifecycle()
    class OnEvent(val event: Event) : Lifecycle()
}

@CalamansiInternal
object LifecycleInvoker {
    context(ExecutionContext) fun invoke(target: Node, lifecycle: Lifecycle) {
        target.invokeLifecycle(lifecycle)
    }
}