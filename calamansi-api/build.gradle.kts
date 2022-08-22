plugins {
    jvm
    kotlin("plugin.serialization")
}

dependencies {
    api(Deps.kotlinSerializationCore)
}