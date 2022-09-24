package calamansi.runtime.resource.loaders

import calamansi.internal.NodeData
import calamansi.meta.CalamansiInternal
import kotlinx.serialization.Serializable


@Serializable
@OptIn(CalamansiInternal::class)
data class SerializedNode constructor(
    val type: String,
    val parent: Int?,
    val data: NodeData,
)

@Serializable
data class SerializedScene(
    val nodes: List<SerializedNode> = emptyList(),
)