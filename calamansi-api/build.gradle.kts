plugins {
    jvm
    id("com.google.devtools.ksp") version Deps.kspVersion
    kotlin("plugin.serialization")
}

dependencies {
    api(Deps.kotlinSerializationCore)
    implementation(Deps.joml)
    ksp(project(":calamansi-symbol-processor"))
}