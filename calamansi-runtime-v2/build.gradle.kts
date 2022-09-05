plugins {
    jvm
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version Deps.kspVersion
}

dependencies {
    implementation(project(":calamansi-api-v2"))
    implementation(Deps.kotlinSerializationJson)
    implementation(Deps.kotlinCoroutines)
    implementation(Deps.lwjgl.core)
    implementation(Deps.lwjgl.glfw)
    implementation(Deps.lwjgl.vulkan)

    runtimeOnly(Deps.lwjgl.runtime.core)
    runtimeOnly(Deps.lwjgl.runtime.glfw)
    runtimeOnly(Deps.lwjgl.runtime.vulkan)

    kspTest(project(":calamansi-symbol-processor-v2"))
}