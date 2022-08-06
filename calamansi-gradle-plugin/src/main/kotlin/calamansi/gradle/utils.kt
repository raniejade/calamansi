package calamansi.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.util.Properties

inline val Project.kotlinJvmExtension: KotlinJvmProjectExtension
    get() {
        return extensions.getByType(KotlinJvmProjectExtension::class.java)
    }

inline val Project.kspExtension: KspExtension
    get() {
        return extensions.getByType(KspExtension::class.java)
    }

object BuildProperties {
    private val properties = Properties()

    init {
        BuildProperties::class.java.classLoader.getResourceAsStream("build.properties")?.use {
            properties.load(it)
        }
    }

    val calamansiVersion by properties
}

fun calamansiDep(dependency: String): String {
    return "com.github.raniejade.calamansi:calamansi-$dependency:${BuildProperties.calamansiVersion}"
}