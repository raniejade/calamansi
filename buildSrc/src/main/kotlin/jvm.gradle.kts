import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xcontext-receivers" // context receivers
    )
}