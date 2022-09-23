package calamansi.runtime.data

import kotlinx.serialization.Serializable

@Serializable
data class ProjectConfig(
    val title: String = "Calamansi Engine",
    val width: Int = 1280,
    val height: Int = 720,
)