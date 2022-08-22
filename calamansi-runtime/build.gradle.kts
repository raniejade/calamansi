plugins {
    jvm
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version Deps.kspVersion
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(Deps.kotlinSerializationJson)

    kspTest(project(":calamansi-symbol-processor"))
}