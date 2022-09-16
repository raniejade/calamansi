plugins {
    jvm
    id("com.google.devtools.ksp") version Deps.kspVersion
    kotlin("plugin.serialization")
}

kotlin {
    val main by sourceSets.getting
    val editor by sourceSets.creating {
        dependsOn(main)
    }

    target.compilations.create("editor") {
        source(editor)
        associateWith(target.compilations["main"])
    }
}

dependencies {
    api(Deps.kotlinSerializationCore)
    api(Deps.joml)

    ksp(project(":calamansi-symbol-processor"))
    "kspEditor"(project(":calamansi-symbol-processor"))
}

enableContextReceivers()