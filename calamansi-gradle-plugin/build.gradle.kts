plugins {
    jvm
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("calamansi") {
            id = "com.github.raniejade.calamansi"
            implementationClass = "calamansi.gradle.CalamansiPlugin"
        }
    }
}

tasks {
    processResources {
        filesMatching("build.properties") {
            expand("calamansiVersion" to rootProject.version)
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("gradle-plugin-api"))
    implementation(kotlin("serialization"))
    implementation(Deps.kspGradlePlugin)
}