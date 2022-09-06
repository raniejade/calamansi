package calamansi.runtime.model

import calamansi.runtime.logging.LogLevel
import kotlinx.serialization.Serializable

@Serializable
data class ProjectConfig(
    val title: String = "Calamansi Engine",
    val width: Int = 1280,
    val height: Int = 720,
    val logLevel: LogLevel = LogLevel.INFO,
)