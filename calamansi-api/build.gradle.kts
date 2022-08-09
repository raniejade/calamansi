plugins {
    jvm
    kotlin("plugin.serialization")
}

dependencies {
    api(Deps.kotlinSerializationCore)
    implementation(Deps.joml)
}