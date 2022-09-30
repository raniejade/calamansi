package calamansi.bus

import calamansi.node.ExecutionContext

typealias MessageListener = context(ExecutionContext) (Message) -> Unit

interface MessageSource {
    interface Subscription {
        fun unsubscribe()
    }

    fun subscribe(listener: MessageListener): Subscription
}