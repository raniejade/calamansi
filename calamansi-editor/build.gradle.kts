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

    // normally applied via the calamans-gradle plugin
    ksp(project(":calamansi-symbol-processor"))
}

application {
    mainClass.set("calamansi.runtime.RuntimeKt")
}