package calamansi.internal

import calamansi.Script
import calamansi.meta.CalamansiInternal
import calamansi.meta.Property
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType

sealed class ScriptPropertyType(val type: KType) {
    class Simple(type: KType) : ScriptPropertyType(type)
    class Enum(type: KType, val possibleValues: Set<kotlin.Enum<*>>)
}

@CalamansiInternal
data class ScriptProperty(
    val name: String,
    val type: ScriptPropertyType,
    private val setter: (ScriptData, Any?) -> Unit,
    private val getter: (ScriptData) -> Any?,
) {
    fun set(data: ScriptData, value: Any?) {
        setter(data, value)
    }

    fun get(data: ScriptData): Any? {
        return getter(data)
    }
}

@CalamansiInternal
interface ScriptDefinition {
    val type: KClass<out Script>
    val properties: Set<ScriptProperty>
    fun create(): Script
    fun applyData(target: Script, data: ScriptData)
    fun extractData(target: Script): ScriptData
    fun serializersModule(): SerializersModule
}

@CalamansiInternal
interface ScriptData {
    val type: KClass<out Script>
}