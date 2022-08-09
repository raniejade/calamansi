package calamansi.runtime.registry

import calamansi.component.Component
import calamansi.logging.Logger
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

class RuntimeRegistry(private val logger: Logger): Registry {
    private val components = mutableMapOf<KClass<*>, ComponentDefinition<*>>()
    private val scripts = mutableMapOf<KClass<*>, ScriptDefinition<*>>()
    val serializersModule by lazy {
        SerializersModule {
            for (definition in components.values) {
                include(definition.serializersModule())
            }
        }
    }

    override fun registerComponent(definition: ComponentDefinition<*>) {
        logger.debug { "Registering component: ${definition.type.qualifiedName}." }
        require(!components.containsKey(definition.type)) { "Component definition for ${definition.type} already exist" }
        components[definition.type] = definition
    }

    override fun registerScript(definition: ScriptDefinition<*>) {
        logger.debug { "Registering script: ${definition.type.qualifiedName}." }
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