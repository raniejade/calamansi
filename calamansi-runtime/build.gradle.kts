plugins {
    jvm
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version Deps.kspVersion
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(project(":calamansi-assets"))
    implementation(Deps.kotlinSerializationJson)
    implementation(Deps.lwjgl.core)
    implementation(Deps.lwjgl.glfw)
    implementation(Deps.lwjgl.opengl)

    runtimeOnly(Deps.lwjgl.runtime.core)
    runtimeOnly(Deps.lwjgl.runtime.glfw)
    runtimeOnly(Deps.lwjgl.runtime.opengl)

    kspTest(project(":calamansi-symbol-processor"))
}

enableContextReceivers()