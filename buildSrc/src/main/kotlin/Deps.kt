object Deps {
    const val jomlVersion = "1.10.4"
    const val kspVersion = "1.7.10-1.0.6"
    const val kotlinSerializationVersion = "1.4.0-RC"
    const val kotlinCoroutinesVersion = "1.6.4"
    const val lwjglVersion = "3.3.1"
    const val skijaVersion = "0.93.6"
    const val sl4jVersion = "2.0.1"
    const val logbackVersion = "1.4.1"
    // TODO: check os
    val lwjglNatives by lazy {
        "natives-macos-arm64"
    }

    // TODO: check os
    val skijaArtifact by lazy {
        "skija-macos-arm64"
    }

    const val joml = "org.joml:joml:$jomlVersion"
    const val ksp = "com.google.devtools.ksp:symbol-processing-api:$kspVersion"
    const val kspGradlePlugin = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion"
    const val kotlinSerializationCore = "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinSerializationVersion"
    const val kotlinSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinSerializationVersion"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion"
    val skija = "org.jetbrains.skija:$skijaArtifact:$skijaVersion"
    const val slf4j = "org.slf4j:slf4j-api:$sl4jVersion"
    const val logback = "ch.qos.logback:logback-classic:$logbackVersion"

    object lwjgl {
        const val core = "org.lwjgl:lwjgl:$lwjglVersion"
        const val glfw = "org.lwjgl:lwjgl-glfw:$lwjglVersion"
        const val vulkan = "org.lwjgl:lwjgl-vulkan:$lwjglVersion"
        const val opengl = "org.lwjgl:lwjgl-opengl:$lwjglVersion"
        const val stb = "org.lwjgl:lwjgl-stb:$lwjglVersion"
        const val yoga = "org.lwjgl:lwjgl-yoga:$lwjglVersion"

        object runtime {
            val core = "org.lwjgl:lwjgl::$lwjglNatives"
            val glfw = "org.lwjgl:lwjgl-glfw::$lwjglNatives"
            val vulkan = "org.lwjgl:lwjgl-vulkan::$lwjglNatives"
            val opengl = "org.lwjgl:lwjgl-opengl::$lwjglNatives"
            val stb = "org.lwjgl:lwjgl-stb::$lwjglNatives"
            val yoga = "org.lwjgl:lwjgl-yoga::$lwjglNatives"
        }
    }
}