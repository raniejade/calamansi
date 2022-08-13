package calamansi.runtime

import calamansi.ExecutionContext
import calamansi.runtime.data.SerializedNode
import calamansi.runtime.data.SerializedScene

class NodeManager(private val componentManager: ComponentManager, private val scriptManager: ScriptManager) {
    context(ExecutionContext) fun buildSceneGraph(scene: SerializedScene): NodeImpl? {
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
    context(ExecutionContext) private fun createNode(config: SerializedNode): NodeImpl {
        val script = config.script?.let { scriptManager.createScriptByQualifiedName(it) }
        require(config.name.isNotBlank()) { "Node has blank name" }
        val node = NodeImpl(config.name, componentManager, script)

        for (data in config.components) {
            val component = node.addComponent(data.type)
            componentManager.applyDataToComponent(component, data)
        }
        return node
    }
}