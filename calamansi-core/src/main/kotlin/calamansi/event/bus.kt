package calamansi.event

import calamansi.node.ExecutionContext

interface EventSubscription {
    fun unsubscribe()
}

class EventBus {
    private val listeners = mutableListOf<context(ExecutionContext) (Event) -> Unit>()

    fun subscribe(listener: context(ExecutionContext) (Event) -> Unit): EventSubscription {
        listeners.add(listener)
        return object : EventSubscription {
            override fun unsubscribe() {
                listeners.remove(listener)
            }
        }
    }

    context(ExecutionContext) fun publish(event: Event) {
        for (listener in listeners) {
            if (event.isConsumed()) {
                break
            }

            listener(this@ExecutionContext, event)
        }
    }
}