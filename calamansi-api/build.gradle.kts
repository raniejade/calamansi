plugins {
    jvm
    id("me.champeau.jmh") version "0.6.6"
    id("com.google.devtools.ksp") version Deps.kspVersion
    kotlin("plugin.serialization")
}

dependencies {
    api(Deps.kotlinSerializationCore)
    ksp(project(":calamansi-symbol-processor"))
}

jmh {
    warmupForks.set(1)
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(1)
}