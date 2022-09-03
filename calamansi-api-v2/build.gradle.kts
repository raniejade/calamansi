
plugins {
    jvm
    id("com.google.devtools.ksp") version Deps.kspVersion
    kotlin("plugin.serialization")
}

dependencies {
    api(Deps.kotlinSerializationCore)
    api(Deps.joml)
    ksp(project(":calamansi-symbol-processor-v2"))
}