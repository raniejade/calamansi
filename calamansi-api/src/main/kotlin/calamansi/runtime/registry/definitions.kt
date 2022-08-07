package calamansi.runtime.registry

import calamansi.component.Component
import calamansi.script.Script
import kotlin.reflect.KClass

interface Definition<T: Any> {
    val type: KClass<T>
    fun create(): T
}

interface ComponentDefinition<T : Component> : Definition<T> {
    val dependencies: List<ComponentDefinition<*>>
    val properties: List<Property<T, *>>
}

interface ScriptDefinition<T : Script> : Definition<T>