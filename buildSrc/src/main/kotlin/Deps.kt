object Deps {
    const val jomlVersion = "1.10.4"
    const val kspVersion = "1.7.10-1.0.6"
    const val kotlinSerializationVersion = "1.4.0-RC"
    const val lwjglVersion = "3.3.1"
    // TODO: check os
    val lwjglNatives by lazy {
        "natives-macos-arm64"
    }

    const val joml = "org.joml:joml:$jomlVersion"
    const val ksp = "com.google.devtools.ksp:symbol-processing-api:$kspVersion"
    const val kspGradlePlugin = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion"
    const val kotlinSerializationCore = "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinSerializationVersion"
    const val kotlinSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinSerializationVersion"

    object lwjgl {
        const val core = "org.lwjgl:lwjgl:$lwjglVersion"
        const val glfw = "org.lwjgl:lwjgl-glfw:$lwjglVersion"
        const val vulkan = "org.lwjgl:lwjgl-vulkan:$lwjglVersion"

        object runtime {
            val core = "org.lwjgl:lwjgl::$lwjglNatives"
            val glfw = "org.lwjgl:lwjgl-glfw::$lwjglNatives"
            val vulkan = "org.lwjgl:lwjgl-vulkan::$lwjglNatives"
        }
    }
}