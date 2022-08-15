package calamansi.runtime.resource

import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.module.Module
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ResourceRefSerializer : KSerializer<ResourceRef<out Resource>> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): ResourceRef<out Resource> {
        return Module.getModule<ResourceModule>().loadResource(decoder.decodeSerializableValue(String.serializer()))
    }

    override fun serialize(encoder: Encoder, value: ResourceRef<out Resource>) {
        encoder.encodeSerializableValue(String.serializer(), value.path)
    }
}