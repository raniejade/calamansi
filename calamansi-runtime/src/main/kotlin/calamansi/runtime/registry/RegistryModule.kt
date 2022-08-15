package calamansi.runtime.registry

import calamansi.Script
import calamansi.component.Component
import calamansi.runtime.Entry
import calamansi.runtime.module.Module
import kotlinx.serialization.modules.SerializersModule
import java.util.*

class RegistryModule : Module() {
    private data class RegistryMetadata(
        val components: Map<String, ComponentDefinition<*>>,
        val scripts: Map<String, ScriptDefinition<*>>,
        val parent: RegistryMetadata?
    ) {
        val serializersModule by lazy {
            SerializersModule {
                for (definition in components.values) {
                    include(definition.serializersModule())
                }
            }
        }

        fun getComponentDefinition(component: String): ComponentDefinition<*> {
            var definition: ComponentDefinition<*>? = null
            var current: RegistryMetadata? = this
            while (current != null) {
                if (current.components.containsKey(component)) {
                    definition = current.components[component]
                    break
                }
                current = current.parent
            }

            checkNotNull(definition) {
                "Failed to fetch definition for component: '$component'"
            }

            return definition
        }

        fun getScriptDefinition(script: String): ScriptDefinition<*> {
            var definition: ScriptDefinition<*>? = null
            var current: RegistryMetadata? = this
            while (current != null) {
                if (current.scripts.containsKey(script)) {
                    definition = current.scripts[script]
                    break
                }
                current = current.parent
            }

            checkNotNull(definition) {
                "Failed to fetch definition for script: '$script'"
            }

            return definition
        }
    }

    private inner class RegistryImpl : Registry {
        val components = mutableMapOf<String, ComponentDefinition<*>>()
        val scripts = mutableMapOf<String, ScriptDefinition<*>>()

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
    }

    private var serializersModule: SerializersModule? = null
    private var registryMetadata: RegistryMetadata? = null


    override fun start() {
        logger.info { "Registry module started." }
    }

    fun pushContext(classLoader: ClassLoader) {
        logger.info { "Loading context using classloader: $classLoader" }
        val loader = ServiceLoader.load(Entry::class.java, classLoader)
        val registry = RegistryImpl()
        for (entry in loader) {
            entry.bootstrap(registry)
        }
        val parent = registryMetadata
        registryMetadata = RegistryMetadata(registry.components.toMap(), registry.scripts.toMap(), parent)
        resetSerializersModule()
    }

    fun popContext() {
        val parent = registryMetadata?.parent
        registryMetadata = parent
        resetSerializersModule()
    }

    fun createScriptInstance(script: String): Script {
        return checkNotNull(registryMetadata).getScriptDefinition(script).create()
    }

    fun createComponentInstance(component: String): Component {
        return checkNotNull(registryMetadata).getComponentDefinition(component).create()
    }

    fun toComponentData(component: Component): ComponentData<*> {
        return checkNotNull(registryMetadata).getComponentDefinition(checkNotNull(component::class.qualifiedName))
            .toData(component)
    }

    fun fromComponentData(data: ComponentData<*>, component: Component) {
        checkNotNull(registryMetadata).getComponentDefinition(checkNotNull(data.type.qualifiedName))
            .fromData(data, component)
    }

    fun getSerializersModule(): SerializersModule {
        if (serializersModule == null) {
            serializersModule = SerializersModule {
                var current: RegistryMetadata? = registryMetadata
                while (current != null) {
                    include(current.serializersModule)
                    current = current.parent
                }
            }
        }
        return serializersModule!!
    }

    private fun resetSerializersModule() {
        serializersModule = null
    }

    override fun shutdown() {
        logger.info { "Registry module shutting down." }
    }
}