
plugins {
    jvm
    id("com.google.devtools.ksp") version Deps.kspVersion
    kotlin("plugin.serialization")
}

dependencies {
    api(Deps.kotlinSerializationCore)
    api(Deps.joml)
    api(Deps.slf4j)

    implementation(Deps.lwjgl.core)
    implementation(Deps.lwjgl.glfw)
    implementation(Deps.lwjgl.opengl)
    implementation(Deps.lwjgl.yoga)
    implementation(Deps.skija)
    implementation(Deps.logback)
    implementation(Deps.kotlinSerializationJson)
    implementation(project(":calamansi-assets"))

    runtimeOnly(Deps.lwjgl.runtime.core)
    runtimeOnly(Deps.lwjgl.runtime.glfw)
    runtimeOnly(Deps.lwjgl.runtime.opengl)
    runtimeOnly(Deps.lwjgl.runtime.yoga)

    ksp(project(":calamansi-symbol-processor"))

}

enableContextReceivers()