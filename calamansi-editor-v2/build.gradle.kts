plugins {
    jvm
    application // for testing only
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version Deps.kspVersion
}

dependencies {
    implementation(project(":calamansi-api-v2"))
    implementation(project(":calamansi-runtime-v2"))
    implementation(Deps.kotlinSerializationJson)

    ksp(project(":calamansi-symbol-processor-v2"))
}

application {
    mainClass.set("calamansi.runtime.EngineKt")
    applicationDefaultJvmArgs += listOf("-XstartOnFirstThread")
}