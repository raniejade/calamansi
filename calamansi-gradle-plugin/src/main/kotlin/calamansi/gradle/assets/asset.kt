package calamansi.gradle.assets

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskContainer

sealed interface Asset : Named {
    @get:InputFile
    val sourceFile: RegularFileProperty
}

interface AssetFactory<T : Asset> {
    fun create(name: String, objectFactory: ObjectFactory): T
}

interface TextureAsset : Asset {
    companion object Factory : AssetFactory<TextureAsset> {
        override fun create(name: String, objectFactory: ObjectFactory): TextureAsset {
            TODO("Not yet implemented")
        }

    }
}

interface MeshAsset : Asset {
    companion object Factory : AssetFactory<MeshAsset> {
        override fun create(name: String, objectFactory: ObjectFactory): MeshAsset {
            TODO("Not yet implemented")
        }

    }
}

interface SceneAsset : Asset {
    companion object Factory : AssetFactory<SceneAsset> {
        override fun create(name: String, objectFactory: ObjectFactory): SceneAsset {
            return objectFactory.newInstance(SceneAsset::class.java, name)
        }
    }
}

object Assets {
    fun registerFactories(container: ExtensiblePolymorphicDomainObjectContainer<Asset>, objectFactory: ObjectFactory) {
        container.registerFactory(SceneAsset::class.java) {
            SceneAsset.create(it, objectFactory)
        }

        container.registerFactory(MeshAsset::class.java) {
            MeshAsset.create(it, objectFactory)
        }

        container.registerFactory(TextureAsset::class.java) {
            TextureAsset.create(it, objectFactory)
        }
    }

    fun get(extension: String): Class<out Asset> {
        return when (extension) {
            "scn" -> SceneAsset::class.java
            else -> throw AssertionError("Unsupported extension: $extension")
        }
    }

    fun createTask(asset: Asset, tasks: TaskContainer): ImportAssetTask {
        val safeName = asset.name.replace("/", "_").replace(".", "_")
        val task = when (asset) {
            is SceneAsset -> {
                tasks.create("importScene-$safeName", ImportSceneTask::class.java)
            }

            else -> throw AssertionError("Unsupported asset: $asset")
        }

        task.sourceFile.set(asset.sourceFile)
        return task
    }
}