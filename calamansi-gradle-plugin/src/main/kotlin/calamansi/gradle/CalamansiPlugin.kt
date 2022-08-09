package calamansi.gradle

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

class CalamansiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(KotlinPlatformJvmPlugin::class.java)
        project.pluginManager.apply(KspGradleSubplugin::class.java)
        project.pluginManager.apply(SerializationGradleSubplugin::class.java)
        project.extensions.create("calamansi", CalamansiExtension::class.java)
        project.configureDefaults()
    }

    private fun Project.configureDefaults() {
        // default repository
        with(repositories) {
            mavenCentral()
        }

        kotlinJvmExtension.target.compilations.getByName("main").apply {
            kotlinOptions.freeCompilerArgs += listOf(
                "-Xcontext-receivers" // context receivers
            )
            dependencies {
                compileOnly(calamansiDep("api"))
            }
            dependencies.add("ksp", calamansiDep("symbol-processor"))
        }
    }
}