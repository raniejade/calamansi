package calamansi.runtime.data

import kotlinx.serialization.Serializable

@Serializable
data class ProjectConfig(
    val width: Int = 720,
    val height: Int = 480,
    val title: String = "Calamansi Engine",
    val defaultScene: String = "assets://default.scn",
)