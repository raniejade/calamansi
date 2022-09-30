package calamansi.bus

import calamansi.node.ExecutionContext

class MessageBus : MessageSource {
    private val listeners = mutableSetOf<MessageListener>()

    override fun subscribe(listener: MessageListener): MessageSource.Subscription {
        listeners.add(listener)
        return object : MessageSource.Subscription {
            override fun unsubscribe() {
                listeners.remove(listener)
            }
        }
    }

    context(ExecutionContext) fun publish(message: Message) {
        for (listener in listeners) {
            listener(this@ExecutionContext, message)
        }
    }
}