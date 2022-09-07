package calamansi.gradle.assets

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.com.google.common.io.Files
import java.io.File
import javax.inject.Inject
import kotlin.io.path.relativeTo

abstract class ImportAssetTask : DefaultTask() {
    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputFile
    abstract val sourceFile: RegularFileProperty

    @get:Internal
    val outputDirectory: Provider<File>
        get() {
            return sourceFile.map {
                val file = it.asFile
                val assetsDir = layout.projectDirectory.dir("src/assets").asFile
                val relativePath = file.toPath().relativeTo(assetsDir.toPath())
                val outputDir = if (relativePath.parent == null) {
                    layout.buildDirectory.dir("assets")
                } else {
                    layout.buildDirectory.dir("assets/" + relativePath.parent.toString())
                }
                outputDir.get().asFile
            }
        }
}

abstract class ImportSceneTask : ImportAssetTask() {
    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun run() {
        fileSystemOperations.copy {
            it.from(sourceFile)
            it.into(outputDirectory)
        }
    }
}