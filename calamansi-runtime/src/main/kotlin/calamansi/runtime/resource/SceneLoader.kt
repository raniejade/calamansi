package calamansi.runtime.resource

import calamansi.Scene
import calamansi.runtime.data.SerializedScene
import java.io.InputStream

class SceneLoader : ResourceLoader<Scene> {
    class SceneImpl(val scene: SerializedScene) : Scene

    override val supportedExtensions: Set<String> = setOf("scn")

    override fun load(
        inputStream: InputStream,
        serializer: Serializer,
    ): LoadedResource<Scene> {
        val serializedScene = serializer.decodeScene(inputStream)
        return LoadedResource(SceneImpl(serializedScene)) {
            // no clean up needed, once this scene is garbage collected every sub resource will be collected as well.
        }
    }
}