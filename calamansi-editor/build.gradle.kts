plugins {
    jvm
    application // for testing only

    // plugins normally applied via the calamansi-gradle plugin
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version Deps.kspVersion
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(project(":calamansi-runtime"))
    implementation(Deps.kotlinSerializationJson)

    // normally applied via the calamansi-gradle plugin
    ksp(project(":calamansi-symbol-processor"))
}

application {
    mainClass.set("calamansi.runtime.EngineKt")
}