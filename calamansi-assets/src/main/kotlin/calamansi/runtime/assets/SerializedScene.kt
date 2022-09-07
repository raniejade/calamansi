package calamansi.runtime.assets

import calamansi.component.Component
import calamansi.internal.ScriptData
import calamansi.meta.CalamansiInternal
import kotlinx.serialization.Serializable


@Serializable
@OptIn(CalamansiInternal::class)
data class SerializedNode constructor(
    val name: String,
    val parent: Int?,
    val script: String?,
    val data: ScriptData?,
    val components: List<Component> = emptyList(),
)

@Serializable
data class SerializedScene(
    val nodes: List<SerializedNode> = emptyList(),
)