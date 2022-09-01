package calamansi.runtime.resource

import calamansi.Node
import calamansi.Scene
import calamansi.runtime.MainDispatcher
import calamansi.runtime.NodeImpl
import calamansi.runtime.data.SerializedNode
import calamansi.runtime.data.SerializedScene
import calamansi.runtime.module.Module
import calamansi.runtime.registry.ComponentData
import calamansi.runtime.registry.RegistryModule
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class SceneLoader : ResourceLoader<Scene> {
    private val logger by lazy {
        Module.getLogger(this::class)
    }

    private inner class SceneImpl(val serializedScene: SerializedScene) : Scene {
        override suspend fun create(preloadResources: Boolean): Node? {
            return buildSceneGraph(serializedScene, preloadResources)
        }
    }

    override val supportedExtensions: Set<String> = setOf("scn")

    override suspend fun load(
        inputStream: InputStream,
    ): LoadedResource<Scene> {
        return coroutineScope {
            val serializedScene =
                Module.getModule<ResourceModule>().getJsonSerializer().decodeFromStream<SerializedScene>(inputStream)
            LoadedResource(SceneImpl(serializedScene)) {
                // no clean up needed, once this scene is garbage collected every sub resource
                // will be collected as well (if there are no other reference to the sub resource).
            }
        }
    }

    private suspend fun buildSceneGraph(scene: SerializedScene, preloadResources: Boolean): NodeImpl? {
        val potentialRoots = mutableListOf<NodeImpl>()
        val nodes = mutableListOf<NodeImpl>()
        // first pass: create all nodes without establishing hierarchy
        for (serializedNode in scene.nodes) {
            val node = createNode(serializedNode, preloadResources)
            nodes.add(node)
        }

        // second pass: establish hierarchy
        for (idx in scene.nodes.indices) {
            val serializedNode = scene.nodes[idx]
            val node = nodes[idx]

            if (serializedNode.parent == null) {
                potentialRoots.add(node)
                continue
            }

            val parent = nodes[checkNotNull(serializedNode.parent)]
            parent.addChild(node)
        }

        if (potentialRoots.size == 0) {
            logger.warn { "No roots detected." }
            return null
        }

        if (potentialRoots.size > 1) {
            logger.warn {
                "Parsed multiple possible root nodes, using ${potentialRoots[0]} as root. Ignoring possibleRoots = ${
                    potentialRoots.subList(
                        1, potentialRoots.size
                    )
                }"
            }
        }

        return potentialRoots[0]
    }

    // only sets component, parent is established in second pass
    private suspend fun createNode(config: SerializedNode, preloadResources: Boolean): NodeImpl {
        check(config.name.isNotBlank()) { "Node has blank name" }
        val node = NodeImpl(config.name, config.script)

        for (data in config.components) {
            val component = node.addComponent(data.type)
            Module.getModule<RegistryModule>().fromComponentData(data, component)
            if (preloadResources) {
                data.preloadResourceRefs()
            }
        }
        return node
    }
}