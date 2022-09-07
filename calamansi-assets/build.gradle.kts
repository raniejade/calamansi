
plugins {
    jvm
    id("com.google.devtools.ksp") version Deps.kspVersion
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":calamansi-api"))
    api(Deps.kotlinSerializationCore)
    implementation(Deps.kotlinSerializationJson)

}

enableContextReceivers()