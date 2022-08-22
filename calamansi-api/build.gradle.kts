plugins {
    jvm
    id("me.champeau.jmh") version "0.6.6"
    kotlin("plugin.serialization")
}

dependencies {
    api(Deps.kotlinSerializationCore)
}

jmh {
    warmupForks.set(1)
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(1)
}