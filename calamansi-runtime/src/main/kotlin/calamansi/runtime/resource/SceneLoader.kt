package calamansi.runtime.resource

import calamansi.Node
import calamansi.Scene
import calamansi.runtime.NodeImpl
import calamansi.runtime.data.SerializedNode
import calamansi.runtime.data.SerializedScene
import calamansi.runtime.module.Module
import calamansi.runtime.registry.RegistryModule
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class SceneLoader : ResourceLoader<Scene> {
    private val logger by lazy {
        Module.getLogger(this::class)
    }

    private inner class SceneImpl(val serializedScene: SerializedScene) : Scene {
        override fun create(): Node? {
            return buildSceneGraph(serializedScene)
        }
    }

    override val supportedExtensions: Set<String> = setOf("scn")

    override fun load(
        inputStream: InputStream,
    ): LoadedResource<Scene> {
        val serializedScene =
            Module.getModule<ResourceModule>().getJsonSerializer().decodeFromStream<SerializedScene>(inputStream)
        return LoadedResource(SceneImpl(serializedScene)) {
            // no clean up needed, once this scene is garbage collected every sub resource
            // will be collected as well (if there are no other reference to the sub resource).
        }
    }

    private fun buildSceneGraph(scene: SerializedScene): NodeImpl? {
        val potentialRoots = mutableListOf<NodeImpl>()
        val nodes = mutableListOf<NodeImpl>()
        // first pass: create all nodes without establishing hierarchy
        for (serializedNode in scene.nodes) {
            val node = createNode(serializedNode)
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
    private fun createNode(config: SerializedNode): NodeImpl {
        check(config.name.isNotBlank()) { "Node has blank name" }
        val node = NodeImpl(config.name, config.script)

        for (data in config.components) {
            val component = node.addComponent(data.type)
            Module.getModule<RegistryModule>().fromComponentData(data, component)
        }
        return node
    }
}