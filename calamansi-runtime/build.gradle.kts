plugins {
    jvm
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(Deps.kotlinSerializationJson)
}