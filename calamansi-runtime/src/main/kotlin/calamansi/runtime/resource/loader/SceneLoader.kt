package calamansi.runtime.resource.loader

import calamansi.Node
import calamansi.Scene
import calamansi.meta.CalamansiInternal
import calamansi.runtime.NodeImpl
import calamansi.runtime.Services
import calamansi.runtime.assets.SerializedNode
import calamansi.runtime.assets.SerializedScene
import calamansi.runtime.logging.LoggingService
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.ResourceService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class SceneLoader : ResourceLoader<Scene> {
    private val resourceService: ResourceService by Services.get()
    private val registryService: RegistryService by Services.get()
    private val logger by lazy {
        Services.getService<LoggingService>().getLogger(SceneLoader::class)
    }

    private inner class SceneImpl(val serializedScene: SerializedScene) : Scene {
        override fun instantiate(): Node? {
            return buildSceneGraph(serializedScene)
        }
    }

    override val supportedExtensions: Set<String> = setOf("scn")

    @OptIn(ExperimentalSerializationApi::class)
    override fun load(
        inputStream: InputStream,
    ): LoadedResource<Scene> {
        val serializedScene =
            resourceService.getJsonSerializer().decodeFromStream<SerializedScene>(inputStream)
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

    // only sets the script, parent is established in second pass
    @OptIn(CalamansiInternal::class)
    private fun createNode(config: SerializedNode): NodeImpl {
        check(config.name.isNotBlank()) { "Node has blank name" }
        val script = config.script?.let(registryService::createScript)
        if (config.data != null) {
            registryService.applyData(script!!, config.data!!)
        }
        config.data?.let { registryService.applyData(script!!, it) }

        return NodeImpl(config.name, script)
    }
}