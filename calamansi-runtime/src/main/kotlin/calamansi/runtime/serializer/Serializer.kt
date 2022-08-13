package calamansi.runtime.serializer

import calamansi.runtime.data.SerializedScene
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import java.io.InputStream
import java.io.OutputStream

class Serializer(module: SerializersModule) {
    private val json = Json {
        serializersModule = module

        // don't encode nulls
        explicitNulls = false
        encodeDefaults = false

        // make serialized files somewhat readable via diffs
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    fun decodeScene(stream: InputStream): SerializedScene {
        return json.decodeFromStream(stream)
    }

    fun encodeScene(scene: SerializedScene, outputStream: OutputStream) {
        json.encodeToStream(scene, outputStream)
    }
}