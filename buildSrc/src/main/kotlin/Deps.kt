object Deps {
    private const val jomlVersion = "1.10.4"
    private const val kspVersion = "1.7.10-1.0.6"
    private const val kotlinSerializationVersion = "1.4.0-RC"

    const val joml = "org.joml:joml:$jomlVersion"
    const val ksp = "com.google.devtools.ksp:symbol-processing-api:$kspVersion"
    const val kspGradlePlugin = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion"
    const val kotlinSerializationCore = "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinSerializationVersion"
    const val kotlinSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinSerializationVersion"
}