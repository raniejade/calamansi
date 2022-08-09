version = "0.1.0"

plugins {
    // TODO: move to buildSrc once Gradle 7.6 is out (Kotlin 1.7.10 support)
    kotlin("plugin.serialization") version "1.7.10" apply false
}

subprojects {
    group = "com.github.raniejade.calamansi"
    version = rootProject.version
}