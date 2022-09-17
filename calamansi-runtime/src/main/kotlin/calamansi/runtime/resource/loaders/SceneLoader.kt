package calamansi.runtime.resource.loaders

import calamansi.meta.CalamansiInternal
import calamansi.node.Node
import calamansi.node.Scene
import calamansi.resource.LoadedResource
import calamansi.resource.Resource
import calamansi.resource.ResourceLoader
import calamansi.runtime.Services
import calamansi.runtime.assets.SerializedNode
import calamansi.runtime.assets.SerializedScene
import calamansi.runtime.logging.LoggingService
import calamansi.runtime.registry.RegistryService
import calamansi.runtime.resource.ResourceService
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import kotlin.reflect.KClass

class SceneLoader : ResourceLoader(setOf("scn.json")) {
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

    override fun loadResource(stream: InputStream, type: KClass<out Resource>, index: Int): LoadedResource {
        val serializedScene =
            resourceService.getJsonSerializer().decodeFromStream<SerializedScene>(stream)
        return LoadedResource(SceneImpl(serializedScene)) {
            // no clean up needed, once this scene is garbage collected every sub resource
            // will be collected as well (if there are no other reference to the sub resource).
        }
    }

    private fun buildSceneGraph(scene: SerializedScene): Node? {
        val potentialRoots = mutableListOf<Node>()
        val nodes = mutableListOf<Node>()
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

    @OptIn(CalamansiInternal::class)
    private fun createNode(config: SerializedNode): Node {
        check(config.type.isNotBlank()) { "Type not specified" }
        val instance = registryService.createNode(config.type)
        registryService.applyData(instance, config.data)
        return instance
    }
}