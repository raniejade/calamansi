package calamansi.runtime.registry

import calamansi.internal.NodeData
import calamansi.internal.NodeDefinition
import calamansi.internal.Registry
import calamansi.meta.CalamansiInternal
import calamansi.node.Node
import calamansi.runtime.service.Service
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicReference

interface Registration {
    fun unregister()
}

@OptIn(CalamansiInternal::class)
class RegistryService : Service {
    private val logger = LoggerFactory.getLogger(RegistryService::class.java)

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

    fun createNode(type: String): Node {
        return getNodeDefinition(type).create()
    }

    fun applyData(target: Node, data: NodeData) {
        getNodeDefinition(checkNotNull(target::class.qualifiedName)).applyData(target, data)
    }

    fun extractData(target: Node): NodeData {
        return getNodeDefinition(checkNotNull(target::class.qualifiedName)).extractData(target)
    }

    private fun getNodeDefinition(type: String): NodeDefinition {
        var definition = coreRegistry.findNodeDefinition(type)
        if (definition != null) {
            return definition
        }

        for ((_, registry) in appRegistries) {
            definition = registry.findNodeDefinition(type)
            if (definition != null) {
                return definition
            }
        }
        throw IllegalStateException("Failed to find definition for $type.")
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
        val loader = ServiceLoader.load(NodeDefinition::class.java)
        for (definition in loader) {
            registry.registerNode(definition)
        }
    }


    private inner class RegistryImpl : Registry {
        private val definitions = mutableMapOf<String, NodeDefinition>()

        fun findNodeDefinition(type: String): NodeDefinition? {
            return definitions[type]
        }

        fun computeSerializersModule() = SerializersModule {
            for ((_, definition) in definitions) {
                include(definition.serializersModule())
            }
        }
        override fun registerNode(definition: NodeDefinition) {
            val qualifiedName = definition.type.qualifiedName!!
            require(!definitions.containsKey(qualifiedName))
            logger.info("Registering node: ${definition.type.qualifiedName}.")
            definitions[qualifiedName] = definition
        }
    }
}