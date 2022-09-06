plugins {
    jvm
    application // for testing only
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version Deps.kspVersion
}

dependencies {
    implementation(project(":calamansi-api"))
    implementation(project(":calamansi-runtime"))
    implementation(Deps.kotlinSerializationJson)

    ksp(project(":calamansi-symbol-processor"))
}

application {
    mainClass.set("calamansi.runtime.EngineKt")
    applicationDefaultJvmArgs += listOf("-XstartOnFirstThread")
}