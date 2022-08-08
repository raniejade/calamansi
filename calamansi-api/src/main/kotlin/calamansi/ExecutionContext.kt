package calamansi

import calamansi.logging.LoggingContext
import kotlin.reflect.KClass

interface ExecutionContext : LoggingContext {
    val Script.owner: Node
    fun Node(script: KClass<out Script>? = null): Node
}