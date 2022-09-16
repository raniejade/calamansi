package calamansi.internal

import calamansi.meta.CalamansiInternal
import calamansi.node.Node
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.KType

sealed class NodePropertyType(val type: KType) {
    class Simple(type: KType) : NodePropertyType(type)
    class Enum(type: KType, val possibleValues: Array<out kotlin.Enum<*>>) : NodePropertyType(type)
}

@CalamansiInternal
data class NodeProperty(
    val name: String,
    val type: NodePropertyType,
    private val setter: (NodeData, Any?) -> Unit,
    private val getter: (NodeData) -> Any?,
) {
    fun set(data: NodeData, value: Any?) {
        setter(data, value)
    }

    fun get(data: NodeData): Any? {
        return getter(data)
    }
}

@CalamansiInternal
interface NodeDefinition {
    val type: KClass<out Node>
    val properties: Set<NodeProperty>
    fun create(): Node
    fun applyData(target: Node, data: NodeData)
    fun extractData(target: Node): NodeData
    fun serializersModule(): SerializersModule
}

@CalamansiInternal
interface NodeData {
    val type: KClass<out Node>
}