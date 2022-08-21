package calamansi.runtime.registry

import calamansi.Script
import calamansi.Component
import calamansi.logging.Logger
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

class RuntimeRegistry(private val logger: Logger) : Registry {
    private val components = mutableMapOf<String, ComponentDefinition<*>>()
    private val scripts = mutableMapOf<String, ScriptDefinition<*>>()
    val serializersModule by lazy {
        SerializersModule {
            for (definition in components.values) {
                include(definition.serializersModule())
            }
        }
    }

    override fun registerComponent(definition: ComponentDefinition<*>) {
        logger.debug { "Registering component: ${definition.type.qualifiedName}." }
        require(!components.containsKey(definition.qualifiedName)) { "Component definition for ${definition.type} already exist" }
        components[definition.qualifiedName] = definition
    }

    override fun registerScript(definition: ScriptDefinition<*>) {
        logger.debug { "Registering script: ${definition.qualifiedName}." }
        require(!scripts.containsKey(definition.qualifiedName)) { "Script definition for ${definition.type} already exist" }
        scripts[definition.qualifiedName] = definition
    }

    fun getComponentDefinition(type: KClass<out Component>): ComponentDefinition<*> {
        return getComponentDefinitionByQualifiedName(checkNotNull(type.qualifiedName))
    }

    fun getComponentDefinitionByQualifiedName(qualifiedName: String): ComponentDefinition<*> {
        return checkNotNull(components[qualifiedName]) {
            "Component definition for $qualifiedName does not exist"
        }
    }

    fun getScriptDefinition(type: KClass<out Script>): ScriptDefinition<out Script> {
        return getScriptDefinitionByQualifiedName(checkNotNull(type.qualifiedName))
    }

    fun getScriptDefinitionByQualifiedName(qualifiedName: String): ScriptDefinition<out Script> {
        return checkNotNull(scripts[qualifiedName]) {
            "Script definition for $qualifiedName does not exist"
        }
    }
}