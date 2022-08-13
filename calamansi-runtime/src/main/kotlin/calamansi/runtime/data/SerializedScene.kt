package calamansi.runtime.data

import calamansi.runtime.registry.ComponentData
import kotlinx.serialization.Serializable


@Serializable
data class SerializedNode(
    val name: String,
    val parent: Int?,
    val script: String?,
    val components: List<ComponentData<*>> = emptyList()
)

@Serializable
data class SerializedScene(
    val nodes: List<SerializedNode> = emptyList()
)