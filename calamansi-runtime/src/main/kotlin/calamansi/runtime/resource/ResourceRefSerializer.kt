package calamansi.runtime.resource

import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ResourceRefSerializer(private val resourceManager: ResourceManager) :
    KSerializer<ResourceRef<out Resource>> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): ResourceRef<out Resource> {
        return resourceManager.loadResource(decoder.decodeSerializableValue(String.serializer()))
    }

    override fun serialize(encoder: Encoder, value: ResourceRef<out Resource>) {
        encoder.encodeSerializableValue(String.serializer(), value.path)
    }
}