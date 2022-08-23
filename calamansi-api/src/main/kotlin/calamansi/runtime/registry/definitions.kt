package calamansi.runtime.registry

import calamansi.Component
import calamansi.Script
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

interface Definition<T : Any> {
    val type: KClass<T>
    val qualifiedName: String
        get() = checkNotNull(type.qualifiedName)

    fun create(): T
}

interface ComponentData<T : Component> {
    val type: KClass<T>
}

interface ComponentDefinition<T : Component> : Definition<T> {
    val dependencies: List<KClass<out Component>>
    val properties: List<Property<T, *>>

    fun toData(component: Component): ComponentData<*>
    fun fromData(data: ComponentData<*>, component: Component)
    fun serializersModule(): SerializersModule
}

interface ScriptDefinition<T : Script> : Definition<T>