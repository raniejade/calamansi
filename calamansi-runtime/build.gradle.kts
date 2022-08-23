plugins {
    jvm
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version Deps.kspVersion
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(Deps.kotlinSerializationJson)
    implementation(Deps.lwjgl.core)
    implementation(Deps.lwjgl.glfw)

    runtimeOnly(Deps.lwjgl.runtime.core)
    runtimeOnly(Deps.lwjgl.runtime.glfw)

    kspTest(project(":calamansi-symbol-processor"))
}