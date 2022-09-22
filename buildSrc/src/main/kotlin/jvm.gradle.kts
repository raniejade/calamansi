import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/skija/maven")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}