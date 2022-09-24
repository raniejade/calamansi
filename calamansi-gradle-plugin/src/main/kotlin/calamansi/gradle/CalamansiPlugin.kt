package calamansi.gradle

import calamansi.gradle.assets.Assets
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin
import kotlin.io.path.extension
import kotlin.io.path.relativeTo

class CalamansiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(KotlinPlatformJvmPlugin::class.java)
        project.pluginManager.apply(KspGradleSubplugin::class.java)
        project.pluginManager.apply(SerializationGradleSubplugin::class.java)
        val extension = project.extensions.create("calamansi", CalamansiExtension::class.java)
        project.configureDefaults()
        project.discoverAssets(extension)
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
                compileOnly(calamansiDep("core"))
            }
            dependencies.add("ksp", calamansiDep("symbol-processor"))
        }
    }

    private fun Project.discoverAssets(calamansiExtension: CalamansiExtension) {
        val importAssets = project.tasks.create("importAssets")
        val assetDir = projectDir.toPath().resolve("src/assets")
        val tree = fileTree(assetDir).matching {
            it.setIncludes(
                listOf(
                    "**/*.scn",
                )
            )
        }

        tree.forEach { file ->
            val relativePath = file.toPath().relativeTo(assetDir)
            val type = Assets.get(relativePath.extension)
            val asset = calamansiExtension.assets.create(relativePath.toString(), type)
            asset.sourceFile.set(file)
            val assetTask = Assets.createTask(asset, project.tasks)
            importAssets.dependsOn(assetTask)
        }
    }
}