package calamansi.runtime

import calamansi.Component
import calamansi.runtime.registry.ComponentData
import calamansi.runtime.registry.RuntimeRegistry
import kotlin.reflect.KClass

class ComponentManager(private val registry: RuntimeRegistry) {
    fun createComponent(component: KClass<out Component>): Component {
        val definition = registry.getComponentDefinition(component)
        return definition.create()
    }

    fun applyDataToComponent(component: Component, data: ComponentData<*>) {
        val definition = registry.getComponentDefinition(component::class)
        definition.fromData(data, component)
    }
}