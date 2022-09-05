package calamansi.runtime.registry

import calamansi.Script
import calamansi.internal.Bootstrap
import calamansi.internal.Registry
import calamansi.internal.ScriptData
import calamansi.internal.ScriptDefinition
import calamansi.meta.CalamansiInternal
import calamansi.runtime.service.Service
import kotlinx.serialization.modules.SerializersModule
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

interface Registration {
    fun unregister()
}

@OptIn(CalamansiInternal::class)
class RegistryService : Service {
    // contains all definitions for the engine, i.e. all core types of the engine
    private val coreRegistry = RegistryImpl()

    // contains definition for the app (a.k.a. the game)
    private val appRegistries = LinkedHashMap<ClassLoader, RegistryImpl>()

    private var cachedSerializersModule = AtomicReference<SerializersModule?>()

    override fun start() {
        findAndLoadBootstrap(coreRegistry)
    }

    fun loadBootstrapFrom(classLoader: ClassLoader): Registration {
        val registry = RegistryImpl()
        findAndLoadBootstrap(registry)
        appRegistries[classLoader] = registry
        return object : Registration {
            override fun unregister() {
                appRegistries.remove(classLoader)
                check(cachedSerializersModule.compareAndSet(cachedSerializersModule.get(), null))
            }
        }
    }

    fun createScript(script: String): Script {
        return getScriptDefinition(script).create()
    }

    fun applyData(target: Script, data: ScriptData) {
        getScriptDefinition(checkNotNull(target::class.qualifiedName)).applyData(target, data)
    }

    fun extractData(target: Script): ScriptData {
        return getScriptDefinition(checkNotNull(target::class.qualifiedName)).extractData(target)
    }

    private fun getScriptDefinition(script: String): ScriptDefinition {
        var definition = coreRegistry.findScriptDefinition(script)
        if (definition != null) {
            return definition
        }

        for ((_, registry) in appRegistries) {
            definition = registry.findScriptDefinition(script)
            if (definition != null) {
                return definition
            }
        }
        throw IllegalStateException("Failed to find definition for $script.")
    }

    override fun stop() {
    }


    fun getSerializersModule(): Pair<Boolean, SerializersModule> {
        var cached = cachedSerializersModule.get()
        if (cached != null) {
            return false to cached
        }

        cached = SerializersModule {
            include(coreRegistry.computeSerializersModule())
            for ((_, registry) in appRegistries) {
                include(registry.computeSerializersModule())
            }
        }

        check(cachedSerializersModule.compareAndSet(null, cached))

        return true to cached
    }


    private fun findAndLoadBootstrap(registry: Registry) {
        val loader = ServiceLoader.load(ScriptDefinition::class.java)
        for (definition in loader) {
            registry.registerScript(definition.type, definition)
        }
    }


    private class RegistryImpl : Registry {
        private val definitions = mutableMapOf<String, ScriptDefinition>()

        override fun registerScript(type: KClass<out Script>, definition: ScriptDefinition) {
            val qualifiedName = type.qualifiedName!!
            require(!definitions.containsKey(qualifiedName))
            println("registring script: $type")
            definitions[qualifiedName] = definition
        }

        fun findScriptDefinition(script: String): ScriptDefinition? {
            return definitions[script]
        }

        fun computeSerializersModule() = SerializersModule {
            for ((_, definition) in definitions) {
                include(definition.serializersModule())
            }
        }
    }
}