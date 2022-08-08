package calamansi.runtime.registry

import calamansi.component.Component
import kotlin.reflect.KClass

class RuntimeRegistry : Registry {
    private val components = mutableMapOf<KClass<*>, ComponentDefinition<*>>()
    private val scripts = mutableMapOf<KClass<*>, ScriptDefinition<*>>()

    override fun registerComponent(definition: ComponentDefinition<*>) {
        require(!components.containsKey(definition.type)) { "Component definition for ${definition.type} already exist" }
        components[definition.type] = definition
    }

    override fun registerScript(definition: ScriptDefinition<*>) {
        require(!scripts.containsKey(definition.type)) { "Script definition for ${definition.type} already exist" }
        scripts[definition.type] = definition
    }

    fun <T : Component> getComponentDefinition(type: KClass<T>): ComponentDefinition<T> {
        return checkNotNull(components[type]) {
            "Component definition for $type does not exist"
        } as ComponentDefinition<T>
    }

    fun <T : Component> getScriptDefinition(type: KClass<T>): ComponentDefinition<T> {
        return checkNotNull(scripts[type]) {
            "Script definition for $type does not exist"
        } as ComponentDefinition<T>
    }
}