package calamansi.runtime.resource

import calamansi.resource.Resource
import calamansi.resource.ResourceRef
import calamansi.runtime.Services
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

@Serializable
data class ResourceRefSurrogate(
    val path: String,
    val type: String,
    val index: Int,
)

class ResourceRefSerializer : KSerializer<ResourceRef<out Resource>> {
    private val resourceService: ResourceService by Services.get()
    override val descriptor: SerialDescriptor = ResourceRefSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): ResourceRef<out Resource> {
        val surrogate = decoder.decodeSerializableValue(ResourceRefSurrogate.serializer())
        val path = surrogate.path
        // TODO: fetch this from registry
        val type = Class.forName(surrogate.type).kotlin
        val index = surrogate.index
        return resourceService.loadResource(path, type as KClass<out Resource>, index)
    }

    override fun serialize(encoder: Encoder, value: ResourceRef<out Resource>) {
        val surrogate = ResourceRefSurrogate(value.path, value.type.qualifiedName!!, value.index)
        encoder.encodeSerializableValue(ResourceRefSurrogate.serializer(), surrogate)
    }
}