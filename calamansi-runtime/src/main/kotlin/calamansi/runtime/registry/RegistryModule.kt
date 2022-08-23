package calamansi.runtime.registry

import calamansi.Component
import calamansi.Script
import calamansi.runtime.module.Module
import kotlinx.serialization.modules.SerializersModule
import java.util.*
import kotlin.reflect.KClass

class RegistryModule : Module() {
    private data class RegistryMetadata(
        val components: Map<String, ComponentDefinition<*>>,
        val scripts: Map<String, ScriptDefinition<*>>,
        val parent: RegistryMetadata?
    ) {
        val componentDependencies: Map<String, Set<KClass<out Component>>> by lazy {
            val builder = mutableMapOf<String, Set<KClass<out Component>>>()
            for ((component, definition) in components) {
                val deps = mutableSetOf<KClass<out Component>>()
                computeDependencies(definition, deps, true)
                builder[component] = deps.toSet()
            }
            builder.toMap()
        }

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

        private fun computeDependencies(
            definition: ComponentDefinition<*>, builder: MutableSet<KClass<out Component>>,
            first: Boolean
        ) {
            if (!first) {
                check(!builder.contains(definition.type)) { "Failed to compute dependencies for ${definition.type}, cycles detected." }
                builder.add(definition.type)
            }
            for (dep in definition.dependencies) {
                computeDependencies(getComponentDefinition(checkNotNull(dep.qualifiedName)), builder, false)
            }
        }
    }

    private inner class RegistryImpl : Registry {
        val components = mutableMapOf<String, ComponentDefinition<*>>()
        val scripts = mutableMapOf<String, ScriptDefinition<*>>()

        override fun registerComponent(definition: ComponentDefinition<*>) {
            logger.info { "Registering component: ${definition.type.qualifiedName}." }
            require(!components.containsKey(definition.qualifiedName)) { "Component definition for ${definition.type} already exist" }
            components[definition.qualifiedName] = definition
        }

        override fun registerScript(definition: ScriptDefinition<*>) {
            logger.info { "Registering script: ${definition.qualifiedName}." }
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
        logger.info { "Loading context using classloader: $classLoader." }
        val loader = ServiceLoader.load(Definition::class.java, classLoader)
        val registry = RegistryImpl()
        for (definition in loader) {
            when (definition) {
                is ScriptDefinition<*> -> registry.registerScript(definition)
                is ComponentDefinition<*> -> registry.registerComponent(definition)
            }
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

    fun getRequiredComponents(component: String): Set<KClass<out Component>> {
        return checkNotNull(registryMetadata).componentDependencies.getValue(component)
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